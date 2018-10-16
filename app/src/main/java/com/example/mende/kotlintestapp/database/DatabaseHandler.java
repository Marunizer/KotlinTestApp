package com.example.mende.kotlintestapp.database;

import android.util.Log;

//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.mongodb.lang.NonNull;
//import com.mongodb.stitch.android.core.Stitch;
//import com.mongodb.stitch.android.core.StitchAppClient;
//import com.mongodb.stitch.android.core.auth.StitchUser;
//import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
//
//public class DatabaseHandler {
//
//    //TODO: Completely remove all MongoDatabse code added: DatabaseHandler.java, mongodb-stitch.xml, gradle implementation
//
//    private final static StitchAppClient client = Stitch.getDefaultAppClient();
//
//    public void accessDatabase()
//    {
//        client.getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(
//                new OnCompleteListener<StitchUser>() {
//                    @Override
//                    public void onComplete(@NonNull final Task<StitchUser> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("myApp", String.format(
//                                    "logged in as user %s with provider %s",
//                                    task.getResult().getId(),
//                                    task.getResult().getLoggedInProviderType()));
//                        } else {
//                            Log.e("myApp", "failed to log in", task.getException());
//                        }
//                    }
//                });
//    }
//
//
//}
