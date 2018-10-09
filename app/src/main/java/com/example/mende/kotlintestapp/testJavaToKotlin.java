package com.example.mende.kotlintestapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

import com.example.mende.kotlintestapp.views.ModelARViewActivity;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;

public class testJavaToKotlin extends AppCompatActivity {

    static void doThis(RecyclerView recyclerView, Context app){
      //  SharedPref.init(getApplicationContext());
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();



//        new ArFragment().getArSceneView().getScene().addOnPeekTouchListener(new Scene.OnPeekTouchListener() {
//            @Override
//            public void onPeekTouch(HitTestResult hitTestResult, MotionEvent motionEvent) {
//                MyGestureDetector().onSingleTapUp(motionEvent)
//            }
//        });
//    }

}}
