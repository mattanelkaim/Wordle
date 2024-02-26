package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WelcomeScreen extends AppCompatActivity implements View.OnClickListener {
    public static final String GAME_TYPE = "GAME_TYPE";
    public static final String DAILY = "DAILY";
    public static final String UNLIMITED = "UNLIMITED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        // Set onClick listeners to both play buttons
        findViewById(R.id.dailyBtn).setOnClickListener(this);
        findViewById(R.id.unlimitedBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String type;
        if (v.getId() == R.id.dailyBtn)
            type = DAILY;
        else if (v.getId() == R.id.unlimitedBtn)
            type = UNLIMITED;
        else
            return; // Do nothing - probably pressed a button that's not a play button

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(GAME_TYPE, type);
        startActivity(intent);
    }

}