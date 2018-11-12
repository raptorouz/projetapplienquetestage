package com.example.yammineeric.dbtest2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Belal on 1/27/2017.
 */

public class ResultatEnqueteAdapter extends ArrayAdapter<ResultatEnquete> {

    //storing all the resultats in the list
    private List<ResultatEnquete> resultats;

    //context object
    private Context context;

    //constructor
    public ResultatEnqueteAdapter(Context context, int resource, List<ResultatEnquete> resultats) {
        super(context, resource, resultats);
        this.context = context;
        this.resultats = resultats;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //getting the layoutinflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //getting listview items
        View listViewItem = inflater.inflate(R.layout.resultats, null, true);
        TextView textViewName = (TextView) listViewItem.findViewById(R.id.textViewName);
        TextView textViewAge = (TextView) listViewItem.findViewById(R.id.textViewAge);
        ImageView imageViewStatus = (ImageView) listViewItem.findViewById(R.id.imageViewStatus);

        //getting the current resultat
        ResultatEnquete resultat = resultats.get(position);

        //setting the name to textview
        textViewName.setText(resultat.getName());
        textViewAge.setText(Integer.toString(resultat.getAge()));

        //if the synced status is 0 displaying
        //queued icon
        //else displaying synced icon
        if (resultat.getStatus() == 0)
            imageViewStatus.setBackgroundResource(R.drawable.stopwatch);
        else
            imageViewStatus.setBackgroundResource(R.drawable.success);

        return listViewItem;
    }
}
