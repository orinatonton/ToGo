package com.togo.togo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;

import com.google.firebase.auth.FirebaseAuth;

public class DriverMenu extends AppCompatActivity {

    GridLayout gridView;

    private FirebaseAuth mAuth;

    CardView mProfile, mTrip, mAdd, mHire, mHistory, mLogout;
    private  FirebaseAuth.AuthStateListener mAth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        gridView = (GridLayout)findViewById(R.id.gridView);

        mProfile = (CardView) findViewById(R.id.edit_profile);
        mTrip = (CardView) findViewById(R.id.trip_history);
        mAdd = (CardView) findViewById(R.id.post_bike);
        mHire = (CardView) findViewById(R.id.hire_bike);
        mHistory = (CardView) findViewById(R.id.hire_history);
        mLogout = (CardView) findViewById(R.id.logout);

        mAuth = FirebaseAuth.getInstance();

        mAth = firebaseAuth -> {

        };

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMenu.this, DriverProfile.class);
                startActivity(intent);
                finish();
            }
        });

        mTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMenu.this, TripHistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMenu.this, PostBikeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mHire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMenu.this, HireBikeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMenu.this, HireBikeHistoryActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMenu.this, SplashActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DriverMenu.this, DriverMapActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAth);
    }
}
