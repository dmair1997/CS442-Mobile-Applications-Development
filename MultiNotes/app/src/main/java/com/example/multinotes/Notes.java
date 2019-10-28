package com.example.multinotes;


public class Notes {
    private String notes_title;
    private String date;
    private String preview;

    public Notes(String notes_title, String date, String preview){
        this.notes_title = notes_title;
        this.date = date;
        this.preview = preview;
    }


    public String getNotes_title() {
        return notes_title;
    }

    public void setNotes_title(String notes_title){
        this.notes_title = notes_title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview){
        this.preview = preview;
    }


    @Override
    public String toString() {
        return "Notes {" + "title = '" + notes_title + '\'' + ", date = '" + date + '\'' + ", text = '" + preview + '\'' + '}';
    }
}
