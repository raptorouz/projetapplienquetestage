package com.example.yammineeric.dbtest2;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /*
    * this is the url to our webservice
    * make sure you are using the ip instead of localhost
    * it will not work if you are using localhost
    * */
    public static final String URL_SAVE_RESULTAT = "http://10.0.2.2/test/test.php";

    //database helper object
    private DatabaseHelper db;

    //View objects
    private Button buttonSave;
    private EditText editTextName;
    private EditText editTextAge;
    private ListView listViewResultats;

    //List to store all the resultats

    //1 means data is synced and 0 means data is not synced
    public static final int RESULTAT_SYNCED_WITH_SERVER = 1;
    public static final int RESULTAT_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "com.example.yammineeric.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private ResultatEnqueteAdapter resultatAdapter;
    private List<ResultatEnquete> resultats;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //initializing views and objects
        db = new DatabaseHelper(this);
        resultats = new ArrayList<>();

        buttonSave = (Button) findViewById(R.id.buttonSave);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        listViewResultats = (ListView) findViewById(R.id.listViewResultats);

        //adding click listener to button
        buttonSave.setOnClickListener(this);

        //calling the method to load all the stored resultats
        loadResultats();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the resultats again
                loadResultats();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }

    /*
    * this method will
    * load the resultats from the database
    * with updated sync status
    * */
    private void loadResultats() {
        resultats.clear();
        Cursor cursor = db.getResultats();
        if (cursor.moveToFirst()) {
            do {
                ResultatEnquete resultat = new ResultatEnquete(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_AGE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS))
                );
                resultats.add(resultat);
            } while (cursor.moveToNext());
        }

        resultatAdapter = new ResultatEnqueteAdapter(this, R.layout.resultats, resultats);
        listViewResultats.setAdapter(resultatAdapter);
    }

    /*
    * this method will simply refresh the list
    * */
    private void refreshList() {
        resultatAdapter.notifyDataSetChanged();
    }

    /*
    * this method is saving the name to ther server
    * */
    private void saveResultatToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving Resultat...");
        progressDialog.show();

        final String name = editTextName.getText().toString().trim();
        final int age = Integer.parseInt(editTextAge.getText().toString().trim());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_RESULTAT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //if there is a success
                                //storing the name to sqlite with status synced
                                saveResultatToLocalStorage(name,age,RESULTAT_SYNCED_WITH_SERVER);
                            } else {
                                //if there is some error
                                //saving the name to sqlite with status unsynced
                                saveResultatToLocalStorage(name,age, RESULTAT_NOT_SYNCED_WITH_SERVER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        //on error storing the name to sqlite with status unsynced
                        saveResultatToLocalStorage(name,age,RESULTAT_NOT_SYNCED_WITH_SERVER);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("age",Integer.toString(age));
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    //saving the name to local storage
    private void saveResultatToLocalStorage(String name,int age,int status) {
        editTextName.setText("");
        db.addResultat(name,age,status);
        ResultatEnquete r = new ResultatEnquete(name,age,status);
        resultats.add(r);
        refreshList();
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Résultats envoyés, temps avant prochain sondage (ou entrer le mot de passe) : ");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View alertboxview = layoutInflater.inflate(R.layout.alertboxlayout, null);
        final EditText pass = (EditText) alertboxview.findViewById(R.id.pass);
        Button btnvld = (Button) alertboxview.findViewById(R.id.btnvld);

        alertDialog.setView(alertboxview);

        alertDialog.setMessage("00:2:00");

        alertDialog.setCancelable(false);//

        buttonSave.setEnabled(false);
        alertDialog.show();
        final CountDownTimer timer = new CountDownTimer(120000, 1000) { //Set Timer for 5 seconds

            public void onTick(long millisUntilFinished) {
                Double mufint = Double.valueOf(millisUntilFinished/60000);

                alertDialog.setMessage("00:"+ ((mufint.intValue()))+":"+((millisUntilFinished%60000))/1000 + " "+ pass.getText().toString());
            }

            @Override
            public void onFinish() {
                alertDialog.cancel();
                buttonSave.setEnabled(true);
            }

        }.start();
        btnvld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String a = pass.getText().toString();
                if(a=="root"){
                    timer.cancel();
                    alertDialog.cancel();
                    buttonSave.setEnabled(true);
                }

            }
        });

    }

    @Override
    public void onClick(View view) {
        saveResultatToServer();
    }
    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }
}
