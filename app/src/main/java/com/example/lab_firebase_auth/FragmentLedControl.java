package com.example.lab_firebase_auth;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentLedControl extends Fragment {

    private static final String TAG = "@@@@@";

    // res string
    private String resRootRef;
    private String resLedState;

    // view
    private TextView tv_msg;
    private Button btn_ledControl;

    // firebase auth
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // firebase database
    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private DatabaseReference ledStateRef;

    private boolean bLedState;



    public FragmentLedControl() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Log.d(TAG, "onCreate()");

        // init resString
        init_resString();

        // init View
        init_view();

        // init Firebase auth
        init_firebaseAuth();

        // init Firebase database
        init_firebaseDatabase();


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_led_control, container, false);
    }


    private void init_resString() {
        Log.d(TAG, "init_resString()");

        resRootRef = getString(R.string.root);
        resLedState = getString(R.string.ledState);
    }

    private void init_view() {
        Log.d(TAG, "init_view()");
//        tv_msg = findViewById(R.id.tv_msg);
//        btn_ledControl = findViewById(R.id.btn_ledControl);
    }

    private void init_firebaseAuth() {
        Log.d(TAG, "init_firebaseAuth()");
        mAuth = FirebaseAuth.getInstance();
    }

    private void init_firebaseDatabase() {
        Log.d(TAG, "init_firebaseDatabase()");
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference(resRootRef);
        ledStateRef = rootRef.child(resLedState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart()");

        // Firebase auth checking
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
//            signInAnonymously();

        // register event listener
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onStart.onDataChange()");
                // Get Post object and use the values to update the UI

                String s = dataSnapshot.child(resLedState).getValue().toString();
                Log.d(TAG, "onDataChange: " + s);

                String str;

                bLedState = Boolean.valueOf(s);
                if (bLedState) {
                    btn_ledControl.setText("TURN OFF");
                    str = "LED State: ON";
                } else {
                    btn_ledControl.setText("TURN ON");
                    str = "LED State: OFF";
                }
                tv_msg.setText(str);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "onStart.onCancelled()", databaseError.toException());
                // ...
            }
        });

        Log.d(TAG, "onStart() - rootRef.toString() - " + rootRef.toString());
    }

    public void click_ledControl(View view) {
        Log.d(TAG, "click_ledControl()");

        bLedState = !bLedState;
//        ledStateRef.setValue(bLedState)
//                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "click_ledControl.onSuccess()");
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "click_ledControl.onFailure()", e);
//                    }
//                })
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Log.d(TAG, "click_ledControl.onComplete()");
//                    }
//                })
//                .addOnCanceledListener(this, new OnCanceledListener() {
//                    @Override
//                    public void onCanceled() {
//                        Log.d(TAG, "click_ledControl.onCanceled()");
//                    }
//                });
    }
}
