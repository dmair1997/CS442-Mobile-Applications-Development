package com.example.multinotes;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AsyncLoadTask extends AsyncTask<String, Void, List<Notes>> {

    private MainActivity mainActivity;

    public AsyncLoadTask(MainActivity ma){

        mainActivity = ma;
    }

    @Override
    protected List<Notes> doInBackground(String... params) {

        String jsonFile;
        List<Notes> noteList = new ArrayList<>();
        try{
            InputStream stream = new FileInputStream(params[0]);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            jsonFile = new String(buffer, "UTF-8");
            JSONArray list = new JSONArray(jsonFile);

            for (int i=0; i<list.length(); i++) {
                JSONObject note = list.getJSONObject(i);
                noteList.add(new Notes(note.getString("TITLE"),
                        note.getString("DATE"), note.getString("PREVIEW")));
            }
        } catch (FileNotFoundException e){
            Toast.makeText(mainActivity.getApplicationContext(), "File not found", Toast.LENGTH_SHORT).show();

        } catch (Exception e){
            e.printStackTrace();
        }
        return noteList;

    }

    @Override
    protected void onPostExecute(List<Notes> notes){
        mainActivity.setView(notes);
    }

}
