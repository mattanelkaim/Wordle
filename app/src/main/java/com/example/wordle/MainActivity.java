package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    final String ANSWERS_FILE = "answers.txt";
    final String WORDLIST_FILE = "wordlist.txt";
    final int WORD_LENGTH = 5;
    final int MAX_GUESSES = 5;
    String secret;
    int guesses = 0;
    int currWordLen = 0;
    public Set<String> wordlist = new HashSet<>();
    enum charScore {GREY, YELLOW, GREEN}

    LinearLayout rowsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            loadWordlist();
            pickSecret();
        }
        catch (IOException e)
        {
            System.out.println("FILE ERROR: " + e);
            Toast.makeText(this, "Error opening files!", Toast.LENGTH_SHORT).show();
        }

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
        if (currWordLen == 4 && letter != ' ') // Index len
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
        if (guesses == MAX_GUESSES)
        {
            Toast.makeText(this, "Max guesses!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currWordLen != WORD_LENGTH)
        {
            Toast.makeText(this, "Please guess a " + WORD_LENGTH + " letter word", Toast.LENGTH_SHORT).show();
            return;
        }

        final String word = getWordFromRow();
        System.out.println("Guess = " + word);
        if (!wordlist.contains(word.toLowerCase()))
        {
            Toast.makeText(this, "Word is not in wordlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        charScore[] wordScore = new charScore[WORD_LENGTH];
        for (int i = 0; i < WORD_LENGTH; i++)
            wordScore[i] = getCharScore(word.charAt(i), i);
        colorScoreWord(view, wordScore);

        guesses++;
        currWordLen = 0;
    }

    public charScore getCharScore(final char ch, final int index) {
        final int position = secret.indexOf(ch);
        if (position == -1)
            return charScore.GREY;
        if (position == index)
            return charScore.GREEN;
        return charScore.YELLOW;
    }

    public void colorScoreWord(View view, final charScore[] scores) {
        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses);

        for (int i = 0; i < WORD_LENGTH; i++)
        {
            TextView letterBox = (TextView) row.getChildAt(i);

            // Handle 2 cases where it's needed to change background
            int bg;
            if (scores[i] == charScore.GREY)
                bg = R.drawable.incorrect_letter;
            else if (scores[i] == charScore.YELLOW)
                bg = R.drawable.kinda_correct_letter;
            else
                bg = R.drawable.correct_letter;

            letterBox.setBackgroundResource(bg);
        }
    }

    public boolean loadWordlist() throws IOException {
        InputStream file = getAssets().open(WORDLIST_FILE);
        InputStreamReader fileReader = new InputStreamReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        wordlist = reader.lines().collect(Collectors.toSet());
        System.out.println(wordlist.size());
        return true;
    }

    public void pickSecret() throws IOException {
        // Get file and count lines
        InputStream file = getAssets().open(ANSWERS_FILE);
        InputStreamReader fileReader = new InputStreamReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        final long lines = reader.lines().count();

        // Randomize line to pick
        Random random = new Random();
        final int n = random.nextInt((int) (lines - 1)) + 1;

        // Read the nth line
        final long offset = (long) (WORD_LENGTH + 1) * (n - 1);
        byte[] data = new byte[WORD_LENGTH];
        file.reset(); // Seek back to top
        file.skip(offset);
        file.read(data);

        secret = new String(data);
        System.out.println("secret = " + secret);
    }
}
