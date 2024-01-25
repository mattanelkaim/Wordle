package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final int WORD_LENGTH = 5;
    //final int MAX_GUESSES = 5;
    int guesses = 0;
    int currWordLen = 0;
    final String secret = "PHONE";

    LinearLayout rowsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rowsContainer = findViewById(R.id.rowsContainer);
        // Button btn = findViewById(R.id.btn);
        // btn.setOnClickListener(this::checkGuess);
    }

    protected String getWordFromRow() {
        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses);
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < WORD_LENGTH; i++)
        {
            final TextView letterBox = (TextView) row.getChildAt(i);
            word.append(letterBox.getText());
        }

        return word.toString();
    }

    public void checkGuess(View view) {
        final String word = getWordFromRow();
        System.out.println("Guess = " + word);
        // Compare to database;
        if (word.equals(secret))
            System.out.println("CONGRATS!!");
    }

    public void setLetter(final char letter) {
        if (currWordLen == 5 && letter != ' ')
            return;
        if (currWordLen == 0 && letter == ' ')
            return;

        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses);
        TextView box = (TextView) row.getChildAt(currWordLen);

        box.setText(letter);

        // Update currWordLen respectively
        if (letter != ' ')
            currWordLen++;
        else
            currWordLen--;
    }

    @Override
    public void onClick(View view) {

    }
}