package com.example.lab_firebase_auth;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab_firebase_auth.model.RgbLedControl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "@@@@@";

    // res string
    private String resRootRef;
    private String resRgbLedState;
    private int resMaxAmount;

    // global var
    private InputFilter[] inputFilters = new InputFilter[]{new InputFilterMinMax("0", "255")};
    private boolean cbAlwaysCheck;
    private TextWatcher textWatcher;

    // view
    private EditText et_red, et_green, et_blue;
    private TextView tv_rgbColor, tv_redMsg, tv_greenMsg, tv_blueMsg;
    private SeekBar sb_red, sb_green, sb_blue;

    // firebase auth
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // firebase database
    private FirebaseDatabase database;
    private DatabaseReference rootRef;
    private DatabaseReference rgbLedStateRef;

    // java class
    private RgbLedControl rgbLedControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate()");

        // init resString
        init_resString();

        // init View
        init_view();

        // init TextWatcher
        init_textWatcher();

        // init Firebase auth
        init_firebaseAuth();

        // init Firebase database
        init_firebaseDatabase();
    }

    private void init_resString() {
        Log.d(TAG, "init_resString()");

        Resources res = getResources();

        resRootRef = res.getString(R.string.root);
        resRgbLedState = res.getString(R.string.rgbLedState);

        resMaxAmount = res.getInteger(R.integer.maxAmount);
    }

    private void init_view() {
        Log.d(TAG, "init_view()");

        et_red = findViewById(R.id.et_redSetting);
        et_red.setFilters(inputFilters);
        et_green = findViewById(R.id.et_greenSetting);
        et_green.setFilters(inputFilters);
        et_blue = findViewById(R.id.et_blueSetting);
        et_blue.setFilters(inputFilters);

        sb_red = findViewById(R.id.sb_red);
        sb_red.setMax(resMaxAmount);
        sb_red.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_green = findViewById(R.id.sb_green);
        sb_green.setMax(resMaxAmount);
        sb_green.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_blue = findViewById(R.id.sb_blue);
        sb_blue.setMax(resMaxAmount);
        sb_blue.setOnSeekBarChangeListener(seekBarChangeListener);

        tv_rgbColor = findViewById(R.id.tv_rgbColor);

        tv_redMsg = findViewById(R.id.tv_redMsg);
        tv_greenMsg = findViewById(R.id.tv_greenMsg);
        tv_blueMsg = findViewById(R.id.tv_blueMsg);
    }

    public void init_textWatcher() {
        Log.d(TAG, "init_textWatcher()");

        Log.d(TAG, "init_textWatcher() - et_redSetting - " + et_red.getText().hashCode());
        Log.d(TAG, "init_textWatcher() - et_greenSetting - " + et_green.getText().hashCode());
        Log.d(TAG, "init_textWatcher() - et_blueSetting - " + et_blue.getText().hashCode());

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged() - " + s + ", " + start + ", " + count + ", " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged() - " + s + ", " + start + ", " + before + ", " + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged() - " + s);

                Log.d(TAG, "afterTextChanged() - s - " + s.hashCode());
                Log.d(TAG, "init_textWatcher() - et_red - " + et_red.getText().hashCode());
                Log.d(TAG, "init_textWatcher() - et_green - " + et_green.getText().hashCode());
                Log.d(TAG, "init_textWatcher() - et_blue - " + et_blue.getText().hashCode());

                SeekBar sb_temp = null;

                if (s.hashCode() == et_red.getText().hashCode())
                    sb_temp = sb_red;
                else if (s.hashCode() == et_green.getText().hashCode())
                    sb_temp = sb_green;
                else if (s.hashCode() == et_blue.getText().hashCode())
                    sb_temp = sb_blue;

                if (s.length() > 0)
                    sb_temp.setProgress(Integer.parseInt(s.toString()));
            }
        };
    }

    private void init_firebaseAuth() {
        Log.d(TAG, "init_firebaseAuth()");

        mAuth = FirebaseAuth.getInstance();
    }

    private void init_firebaseDatabase() {
        Log.d(TAG, "init_firebaseDatabase()");

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference(resRootRef);
        rgbLedStateRef = rootRef.child(resRgbLedState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart()");

        // Firebase auth checking
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            signInAnonymously();

        // register event listener
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onStart.onDataChange()");
                // Get Post object and use the values to update the UI

                String s = dataSnapshot.child(resRgbLedState).getValue().toString();
                Log.d(TAG, "onDataChange: " + s);

                rgbLedControl = dataSnapshot.child(resRgbLedState).getValue(RgbLedControl.class);
                Log.d(TAG, "onDataChange() - rgbLedControl -" +
                        " R: " + rgbLedControl.R +
                        ", G: " + rgbLedControl.G +
                        ", B: " + rgbLedControl.B);

                tv_rgbColor.setBackgroundColor(Color.rgb(rgbLedControl.R, rgbLedControl.G, rgbLedControl.B));

                tv_rgbColor.setTextColor(Color.rgb(reverseIntHex(rgbLedControl.R), reverseIntHex(rgbLedControl.G), reverseIntHex(rgbLedControl.B)));
                tv_rgbColor.setText(String.format("R: %d, G: %d, B: %d", rgbLedControl.R, rgbLedControl.G, rgbLedControl.B));

                if (cbAlwaysCheck) {
                    colorSync(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "onStart.onCancelled()", databaseError.toException());
            }
        });

        Log.d(TAG, "onStart() - rootRef.toString() - " + rootRef.toString());

        et_red.addTextChangedListener(textWatcher);
        et_green.addTextChangedListener(textWatcher);
        et_blue.addTextChangedListener(textWatcher);
    }

    private void signInAnonymously() {
        Log.d(TAG, "signInAnonymously()");

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInAnonymously.onComplete: success");
                        } else {
                            Log.w(TAG, "signInAnonymously.onComplete: failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "signInAnonymously.onFailure(): failure", e);
                    }
                });
    }

    public void colorSet(View view) {
        Log.d(TAG, "colorSet()");

        rgbLedControl.R = Integer.parseInt(et_red.getText().toString());
        rgbLedControl.G = Integer.parseInt(et_green.getText().toString());
        rgbLedControl.B = Integer.parseInt(et_blue.getText().toString());

        rgbLedStateRef.setValue(rgbLedControl);
    }

    public void colorSync(View view) {
        Log.d(TAG, "colorSync()");

        et_red.setText(String.valueOf(rgbLedControl.R));
        et_green.setText(String.valueOf(rgbLedControl.G));
        et_blue.setText(String.valueOf(rgbLedControl.B));

//        sb_red.setProgress(rgbLedControl.R);
//        sb_green.setProgress(rgbLedControl.G);
//        sb_blue.setProgress(rgbLedControl.B);
    }

    private int reverseIntHex(int i) {
        Log.d(TAG, "reverseIntHex()" + (i ^ 0xff));
        return i ^ 0xff;
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.d(TAG, "onProgressChanged() - " + seekBar + ", " + progress + ", " + fromUser);

            switch (seekBar.getId()) {
                case R.id.sb_red:
                    et_red.setText(String.format("%d", progress));
                    break;
                case R.id.sb_green:
                    et_green.setText(Integer.toString(progress));
                    break;
                case R.id.sb_blue:
                    et_blue.setText(Integer.toString(progress));
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStartTrackingTouch() - " + seekBar);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.d(TAG, "onStopTrackingTouch() - " + seekBar);
        }
    };

    public void clickAlwaysSync(View view) {
        cbAlwaysCheck = ((CheckBox)view).isChecked();
    }
}
