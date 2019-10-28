package com.example.stockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StockAppDB";
    private static final String TABLE_NAME = "StockWatchTable";
    private static final String SYMBOL = "StockSymbol";
    private static final String COMPANY = "CompanyName";
    private static final String ID = "id";
    private static final int DATABASE_VERSION = 1;


    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME +" ("+
                    SYMBOL +" TEXT not null unique," +
                    COMPANY +" TEXT not null, " +
                    ID + " TEXT not null)";

    private SQLiteDatabase database;

    public DatabaseHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addStock (Stock stock){
        ContentValues values = new ContentValues();
        values.put(SYMBOL, stock.getSymbol());
        values.put(COMPANY, stock.getSymbol());
        values.put(ID, stock.getId());

        database.insert(TABLE_NAME,null,values);

    }



    public void deleteStock (String symbol){
        int x = database.delete(TABLE_NAME, SYMBOL + " = ?", new String[] { symbol });
    }

    public ArrayList<Stock> loadStocks() {

        ArrayList<Stock> stocks = new ArrayList<>();
        Cursor cursor = database.query(
                TABLE_NAME, // The table to query
                new String[]{SYMBOL, COMPANY, ID}, // The columns to return
                null, // The columns for the WHERE clause
                null, // The values for the WHERE clause
                null, // don't group the rows
                null, // don't filter by row groups
                null); // The sort order

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String symbol = cursor.getString(0);
                String company = cursor.getString(1);
                String id = cursor.getString(2);

                Stock c = new Stock(company, symbol, id);
                stocks.add(c);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return stocks;
    }


    public void destroy(){
        database.close();
    }


}
