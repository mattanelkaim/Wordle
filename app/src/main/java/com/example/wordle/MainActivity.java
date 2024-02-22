package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final String ANSWERS_FILE = "answers.txt";
    final String WORDLIST_FILE = "wordlist.txt";
    final int WORD_LENGTH = 5;
    final int MAX_GUESSES = 6;
    String secret;
    int guesses = 0;
    int currWordLen = 0;
    public Set<String> wordlist = new HashSet<>();
    final int INVALID_LINE = -1; // ERROR CODE

    enum charScore {GREY, YELLOW, GREEN}

    LinearLayout rowsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            loadWordlist();
            //secret = pickSecret();
            secret = generatePerDay();
            System.out.println("secret = " + secret);
        }
        catch (IOException e)
        {
            System.out.println("FILE ERROR: " + e);
            Toast.makeText(this, "Error opening files!", Toast.LENGTH_SHORT).show();
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("Hashing problem: " + e);
            Toast.makeText(this, "Hashing problem", Toast.LENGTH_SHORT).show();
        }

        setKeyboardListeners();
        rowsContainer = findViewById(R.id.rowsContainer);
    }

    public void setKeyboardListeners() {
        LinearLayout row1 = findViewById(R.id.keyboardRow1);
        LinearLayout row2 = findViewById(R.id.keyboardRow2);
        LinearLayout row3 = findViewById(R.id.keyboardRow3);

        for (int i = 0; i < 10; i++)
        {
            row1.getChildAt(i).setOnClickListener(this);
        }
        for (int i = 0; i < 9; i++)
        {
            row2.getChildAt(i).setOnClickListener(this);
        }
        for (int i = 1; i < 8; i++)
        {
            row3.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        final String clickedValue = ((TextView)v).getText().toString();
        if (clickedValue.length() == 1)
            setLetter(clickedValue.charAt(0));
    }

    /* ################ GET & MODIFY GUESS ################ */

    protected String getWordFromRow() {
        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses + 1); // +1 to skip answer box
        StringBuilder word = new StringBuilder();

        for (int i = 0; i < WORD_LENGTH; i++)
        {
            final TextView letterBox = (TextView) row.getChildAt(i);
            word.append(letterBox.getText());
        }

        return word.toString();
    }

    public void setLetter(final char letter) {
        if (guesses >= MAX_GUESSES)
            return;
        if (letter != ' ' && currWordLen == 5) // Index len
            return;
        if (letter == ' ' && --currWordLen == -1) // Decrease len to delete correctly
        {
            currWordLen = 0; // Update back
            return;
        }

        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses + 1); // +1 to skip answer box
        TextView box = (TextView) row.getChildAt(currWordLen); // Normalize to index

        System.out.println("Found box: " + box.getText().toString());
        box.setText(String.valueOf(letter));

        // Update currWordLen respectively
        if (letter != ' ')
            currWordLen++;

        System.out.println(currWordLen);
    }

    public void delete(View view)
    {
        setLetter(' ');
    }

    /* ################ GUESS CHECKING & WIN/LOSE LOGIC ################ */

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
        if (!wordlist.contains(word.toLowerCase())) // Words in wordlist are lowercase
        {
            Toast.makeText(this, "Word is not in wordlist!", Toast.LENGTH_SHORT).show();
            return;
        }

        charScore[] wordScore = new charScore[WORD_LENGTH];
        String tempSecret = secret;
        for (int i = 0; i < WORD_LENGTH; i++)
        {
            final char ch = word.charAt(i);
            wordScore[i] = getCharScore(tempSecret, ch, i);
            tempSecret = tempSecret.replaceFirst(String.valueOf(ch), " ");
        }
        colorScoreWord(wordScore);
        if (Arrays.stream(wordScore).allMatch(i -> i == charScore.GREEN))
        {
            win();
            return;
        }

        if (++guesses >= MAX_GUESSES)
            lose();

        currWordLen = 0;
    }

    public void lose() {
        System.out.println("player lost");
        final GridLayout answerBox = findViewById(R.id.answer);
        TextView answer = (TextView) answerBox.getChildAt(1);
        answer.setText(secret);
        answer.setVisibility(View.VISIBLE);
    }

    public void win() {
        System.out.println("player win");
        GridLayout answerBox = findViewById(R.id.answer);
        TextView answer = (TextView) answerBox.getChildAt(1);
        answer.setText("You win nigga!");
        answerBox.setVisibility(View.VISIBLE);
        answerBox.getChildAt(0).setVisibility(View.INVISIBLE);
        guesses = MAX_GUESSES;
    }

    /* ################ HANDLE GUESS SCORING ################ */

    public charScore getCharScore(final String str, final char ch, final int index) {
        final int position = str.indexOf(ch);
        System.out.println(ch + " pos: " + position);
        if (position == -1)
            return charScore.GREY;
        if (position == index)
            return charScore.GREEN;
        return charScore.YELLOW;
    }

    public void colorScoreWord(final charScore[] scores) {
        final LinearLayout row = (LinearLayout) rowsContainer.getChildAt(guesses + 1); // +1 to skip answer box

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

    /* ################ FILES & WORDLIST ################ */

    public void loadWordlist() throws IOException {
        InputStream file = getAssets().open(WORDLIST_FILE);
        InputStreamReader fileReader = new InputStreamReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        wordlist = reader.lines().collect(Collectors.toSet());
    }

    public String pickSecret() throws IOException {
        return pickSecret(INVALID_LINE);
    }

    public String pickSecret(int line) throws IOException {
        // Get file and count lines
        InputStream file = getAssets().open(ANSWERS_FILE);
        InputStreamReader fileReader = new InputStreamReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        final long lines = reader.lines().count();

        // Randomize line to pick
        if (line == INVALID_LINE)
        {
            Random random = new Random();
            line = random.nextInt((int) (lines - 1)) + 1;
        }

        // Read the nth line
        final long offset = (long) (WORD_LENGTH + 1) * (line % lines - 1);
        byte[] data = new byte[WORD_LENGTH];
        file.reset(); // Seek back to top
        final long skipped = file.skip(offset);
        final long read = file.read(data);
        if (skipped != offset || read != WORD_LENGTH)
            throw new IOException("ERROR: Skip/read failed");

        return new String(data).toUpperCase(); // Words in answers file are lowercase
    }

    public String generatePerDay() throws IOException, NoSuchAlgorithmException {
        final LocalDate currentDate = LocalDate.now();

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(currentDate.toString().getBytes());
        final byte[] digest = md5.digest();
        final ByteBuffer buffer = ByteBuffer.wrap(digest);
        final int randomInt = buffer.getInt();
        System.out.println(randomInt);
        return pickSecret(randomInt);
    }
}
