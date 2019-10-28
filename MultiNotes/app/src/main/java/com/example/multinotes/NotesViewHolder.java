package com.example.multinotes;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class NotesViewHolder extends RecyclerView.ViewHolder {

    public TextView notes_title;
    public TextView date;
    public TextView preview;

    public NotesViewHolder(View view){
        super(view);

        notes_title = view.findViewById(R.id.notes_title);
        date = view.findViewById(R.id.date);
        preview = view.findViewById(R.id.preview);

    }

}
