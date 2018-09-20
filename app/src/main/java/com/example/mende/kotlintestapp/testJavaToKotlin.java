package com.example.mende.kotlintestapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class testJavaToKotlin extends AppCompatActivity {

    static void doThis(RecyclerView recyclerView, Context app){
      //  SharedPref.init(getApplicationContext());
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
    }

}
