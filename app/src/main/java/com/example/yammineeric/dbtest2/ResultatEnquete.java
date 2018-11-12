package com.example.yammineeric.dbtest2;

/**
 * Created by Belal on 1/27/2017.
 */

public class ResultatEnquete {
    private int age;
    private String name;
    private int status;

    public ResultatEnquete(String name,int age, int status) {
        this.name = name;
        this.age = age;
        this.status = status;
    }

    public String getName() {
        return name;
    }
    public int getAge(){return age;};
    public int getStatus() {
        return status;
    }
}
