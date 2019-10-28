package com.example.multinotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private List<Notes> notesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;


    private static final int EDIT_ACT = 1;

    private boolean newNote = true;

    AsyncLoadTask aLoadTask = new AsyncLoadTask(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File f = new File(getApplicationContext().getFilesDir() + "/data.json");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (Exception e) {

            }
        }
        aLoadTask.execute(getApplicationContext().getFilesDir() + "/data.json");


    }

    @Override
    protected void onPause() {
        saveFile();
        super.onPause();
    }

    @Override
    protected void onResume(){
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        notesAdapter = new NotesAdapter(notesList, this);

        recyclerView.setAdapter(notesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_menu:
                Intent intentAbout = new Intent(MainActivity.this, About_activity.class);
                startActivity(intentAbout);
                return true;
            case R.id.add_menu:
                Intent intentAdd = new Intent(MainActivity.this, Edit_Activity.class);
                newNote = true;
                startActivityForResult(intentAdd, EDIT_ACT);
                return true;
            default:
                return false;
        }

    }

    @Override
    public void onClick(View view) {
        //Toast.makeText(this, "short click!", Toast.LENGTH_SHORT).show();

        int pos = recyclerView.getChildLayoutPosition(view);
        Notes note = notesList.get(pos);

        Intent intentE = new Intent(MainActivity.this, Edit_Activity.class);
        intentE.putExtra("NOTE_POS", pos);
        intentE.putExtra("NOTE_TITLE", note.getNotes_title());
        intentE.putExtra("NOTE_PREVIEW", note.getPreview());
        intentE.putExtra("NEW_NOTE", "false");
        startActivityForResult(intentE, EDIT_ACT);

    }

    @Override
    public boolean onLongClick(View view) {
        //Toast.makeText(this, "long click!", Toast.LENGTH_SHORT).show();

        final int pos = recyclerView.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeNote(pos);
                //Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setMessage("Delete " + notesList.get(pos).getNotes_title() + "?");

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_ACT) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("SAVE_NOTE", false)) {
                    if (Integer.parseInt(data.getStringExtra("NOTE_POS")) >= 0 && notesList.size() > 0 && !newNote) {
                        removeNote(Integer.parseInt(data.getStringExtra("NOTE_POS")));
                        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
                }
                addNote(new Notes(data.getStringExtra("NOTE_TITLE"), data.getStringExtra("NOTE_DATE"), data.getStringExtra("NOTE_PREVIEW")));
                newNote = false;
                //Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
        }

    }

    public void removeNote (int pos){
        notesList.remove(pos);
        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
        notesAdapter.notifyDataSetChanged();
    }

    public void addNote(Notes notes){
        notesList.add(0, notes);
        notesAdapter.notifyDataSetChanged();
    }

    public void setView(List<Notes> notes){
        notesList = notes;
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        notesAdapter = new NotesAdapter(notesList, this);

        recyclerView.setAdapter(notesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void saveFile() {

        JSONArray list = new JSONArray();
        for (int i=0; i<notesList.size(); i++) {
            JSONObject note = new JSONObject();
            try {
                note.put("TITLE", notesList.get(i).getNotes_title());
                note.put("PREVIEW", notesList.get(i).getPreview());
                note.put("DATE", notesList.get(i).getDate());
                list.put(note);
            } catch (Exception e) {

            }
        }
        try {
            Writer output = null;
            File file = new File(getApplicationContext().getFilesDir() + "/data.json");
            output = new BufferedWriter(new FileWriter(file));
            output.write(list.toString());
            output.close();
            //Toast.makeText(getApplicationContext(), "File Saved", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void readFile(){
        String json;
        try {
            InputStream is = getApplicationContext().openFileInput("data.json");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
            JSONArray list = new JSONArray(json);

            for (int i=0; i<list.length(); i++) {
                JSONObject note = list.getJSONObject(i);
                notesList.add(new Notes(note.getString("TITLE"), note.getString("DATE"), note.getString("PREVIEW")));
            }

        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not present!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
