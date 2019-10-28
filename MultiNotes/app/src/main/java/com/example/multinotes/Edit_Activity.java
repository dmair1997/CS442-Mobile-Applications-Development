package com.example.multinotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Edit_Activity extends AppCompatActivity {

    public EditText note_name;
    public EditText text_content;

    public String prevTitle;
    public String prevContent;
    public int nPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_);

        note_name = (EditText) findViewById(R.id.note_name);
        text_content = (EditText) findViewById(R.id.text_content);

        Intent intent = getIntent();
        if (intent.hasExtra("NOTE_TITLE") && intent.hasExtra("NOTE_PREVIEW")) {
            prevTitle = intent.getStringExtra("NOTE_TITLE");
            prevContent = intent.getStringExtra("NOTE_PREVIEW");
            //nPos = Integer.parseInt(intent.getStringExtra("NOTE_POS"));
            note_name.setText(prevTitle);
            text_content.setText(prevContent);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save:
                Intent data = new Intent();
                if (note_name.getText().toString().length() != 0) {
                    if (!note_name.getText().toString().equals(prevTitle) || !text_content.getText().toString().equals(prevContent)) {
                        data.putExtra("NOTE_TITLE", note_name.getText().toString());
                        data.putExtra("NOTE_PREVIEW", text_content.getText().toString());
                        data.putExtra("NOTE_DATE", new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date()));
                        data.putExtra("SAVE_NOTE", true);
                        data.putExtra("NOTE_POS", String.valueOf(nPos));
                    } else {
                        //data.putExtra("NOTE_POS", String.valueOf(-1));
                        //data.putExtra("SAVE_NOTE", false);
                        finish();

                    }
                    setResult(RESULT_OK, data);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Untitled note not saved", Toast.LENGTH_LONG).show();
                    finish();
                }

                return true;
            default:
                return false;


        }
    }

    @Override
    public void onBackPressed() {

        if (note_name.getText().toString().length() != 0) {
            if (!note_name.getText().toString().equals(prevTitle) || !text_content.getText().toString().equals(prevContent)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent data = new Intent();
                        data.putExtra("NOTE_TITLE", note_name.getText().toString());
                        data.putExtra("NOTE_PREVIEW", text_content.getText().toString());
                        data.putExtra("NOTE_DATE", new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date()));
                        data.putExtra("SAVE_NOTE", true);
                        data.putExtra("NOTE_POS", String.valueOf(nPos));
                        setResult(RESULT_OK, data);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

                builder.setMessage("Note not saved. Save?");

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Untitled note not saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
