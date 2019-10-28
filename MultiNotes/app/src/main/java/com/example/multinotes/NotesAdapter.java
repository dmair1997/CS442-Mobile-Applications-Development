package com.example.multinotes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder> {

    //private static final String TAG = "NotesAdapter";
    private List<Notes> notesList;
    private MainActivity mainAct;

    public NotesAdapter(List<Notes> nList, MainActivity ma){
        this.notesList = nList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder: MAKING NEW MyViewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new NotesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder: FILLING VIEW HOLDER Employee " + position);

        Notes notes = notesList.get(position);

        holder.notes_title.setText(notes.getNotes_title());
        holder.date.setText(notes.getDate());

        if (notes.getNotes_title().length() >= 80) {
            holder.preview.setText(notes.getPreview().substring(0,80) + "...");
        } else {
            holder.preview.setText(notes.getPreview());
        }

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }
}
