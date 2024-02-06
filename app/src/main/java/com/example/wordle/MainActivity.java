package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    final int WORD_LENGTH = 5;
    //final int MAX_GUESSES = 5;
    int guesses = 0;
    int currWordLen = 0;
    final String secret = "PHONE";
    enum charScore {GREY, YELLOW, GREEN}

    LinearLayout rowsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rowsContainer = findViewById(R.id.rowsContainer);
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

    public void checkGuess(View view) {
        final String word = getWordFromRow();
        System.out.println("Guess = " + word);
        // Compare to database;

        charScore[] wordScore = new charScore[WORD_LENGTH];
        for (int i = 0; i < WORD_LENGTH; i++)
            wordScore[i] = getCharScore(word.charAt(i), i);
        colorScoreWord(view, wordScore);

        guesses++;
    }

    public charScore getCharScore(final char ch, final int index) {
        final int position = secret.indexOf(ch);
        if (position == -1)
            return charScore.GREY;
        if (position == index)
            return charScore.GREEN;
        return charScore.YELLOW;
    }

    public void colorScoreWord(View view, charScore[] scores) {
        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses);

        for (int i = 0; i < WORD_LENGTH; i++)
        {
            TextView letterBox = (TextView) row.getChildAt(i);
            if (scores[i] == charScore.GREY)
                continue;

            // Handle 2 cases where it's needed to change background
            final int bg = (scores[i] == charScore.GREEN) ? R.drawable.correct_letter : R.drawable.kinda_correct_letter;
            letterBox.setBackgroundResource(bg);
        }
    }
}
