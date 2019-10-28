package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static String urltag = "http://www.marketwatch.com/investing/stock/";

    private ArrayList<Stock> stockList = new ArrayList<Stock>();

    private SwipeRefreshLayout swiper;
    private RecyclerView recycler;
    private stockAdapter stockAdapter1;
    private DatabaseHandler databaseHandler;

    private static MainActivity ma;

    public ArrayList<Stock>fStocks = new ArrayList<>();
    
    private static final String TAG = "On open";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ma = this;

        recycler = findViewById(R.id.recycler);

        stockAdapter1 = new stockAdapter(stockList, this);

        recycler.setAdapter(stockAdapter1);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

               doRefresh();
            }
        });

        databaseHandler = new DatabaseHandler(this);

        if(networkCheck() == false){
            AlertDialog.Builder noNet = new AlertDialog.Builder(this);
            noNet.setTitle("Not connected to network");
            noNet.show();
        }

        //ArrayList<Stock> stocks = databaseHandler.loadStocks();


        fetchStocks();

        //stockList.add(new Stock("Amazon inc.", "AMZN", 78, 39, 31,"Yuh"));
        //stockList.add(new Stock("Apple inc.", "AAPL", 39, 39, 31, "Naw"));
    }

    private void doRefresh(){
        if (networkCheck()){
            initRecycle(new ArrayList<Stock>());
            stockList.clear();
            fetchStocks();
            swiper.setRefreshing(false);
        }
        else{
            swiper.setRefreshing(false);
            AlertDialog.Builder noNet = new AlertDialog.Builder(this);
            noNet.setTitle("Not connected to network");
            noNet.show();
        }
    }

    public void initRecycle(ArrayList<Stock> stockslist){
        stockAdapter1.notifyDataSetChanged();
    }

    public Boolean networkCheck(){

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();


        if (cm == null){
            Toast.makeText(this, "No access to ConnectivityManager",Toast.LENGTH_SHORT).show();
            return false;
        }
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return  true;
        } else {
            return  false;
        }
    }

    public void fetchStocks(){
        ArrayList<Stock> list = databaseHandler.loadStocks();
        stockList.addAll(list);
        stockAdapter1.notifyDataSetChanged();
        new AsyncData(this).execute(stockList);
    }

    @Override
    protected void onResume() {
        
        stockAdapter1.notifyDataSetChanged();

        Log.d(TAG, "onResume: ");
        new AsyncStockLoader(this).execute("https://api.iextrading.com/1.0/ref-data/symbols");

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stock_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.addStockMenu:
                addButtonPressed();
            default:
                return false;
                //return super.onOptionsItemSelected();
        }

    }

    public boolean addButtonPressed(){
        if (networkCheck()) {
            final MainActivity main = this;
            final EditText field = new EditText(this);

            AlertDialog.Builder adb = new AlertDialog.Builder(this);

            field.setInputType(InputType.TYPE_CLASS_TEXT);
            field.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            field.setGravity(Gravity.CENTER_HORIZONTAL);

            adb.setView(field);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    String stockQuery = field.getText().toString();
                    ArrayList<Stock> result = searchStocks(stockQuery);
                    showOptionsDialog(result, stockQuery);
                }
            });
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled add stock
                }
            });
            adb.setTitle("Stock Selection");
            adb.setMessage("Please enter a Stock Symbol:");
            AlertDialog dialog = adb.create();
            dialog.show();
        } else{
            AlertDialog.Builder net = new AlertDialog.Builder(this);
            net.setTitle("Not connected to network");
            net.show();
        }
        return true;
    }

    public void showOptionsDialog(final ArrayList<Stock> fetchedStocks, String query){
        android.app.AlertDialog.Builder builderSingle = new android.app.AlertDialog.Builder(this);
        builderSingle.setTitle("Make A Selection");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for(int i =0; i < fetchedStocks.size(); i++){
            arrayAdapter.add(fetchedStocks.get(i).getName());
        }

        builderSingle.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Stock stock =  new Stock(
                        fetchedStocks.get(which).getName(),
                        fetchedStocks.get(which).getSymbol(),
                        fetchedStocks.get(which).getId()
                );
                addNewStock(stock);
            }
        });
        if(fetchedStocks.size() >0){
            builderSingle.show();
        }
        else{
            android.app.AlertDialog.Builder noneFound = new android.app.AlertDialog.Builder(this);
            noneFound.setTitle("Symbol not Found: " + query);
            noneFound.show();
        }
    }



    @Override
    public void onClick(View view) {

        final int pos = recycler.getChildLayoutPosition(view);

        Stock stock = stockList.get(pos);

        String url = urltag+stock.getSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View view) { // Needs to be edited

        final int pos = recycler.getChildLayoutPosition(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                databaseHandler.deleteStock(stockList.get(pos).getSymbol());
                stockList.remove(pos);
                stockAdapter1.notifyDataSetChanged();
                //removeStock(pos);
                //Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        builder.setMessage("Delete " + stockList.get(pos).getSymbol().toUpperCase() + "?");

        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    public void addStock(Stock stock){
        if(stock != null) {

            stockList.add(stock);
            Collections.sort(stockList, new Comparator<Stock>() {
                @Override
                public int compare(Stock t0, Stock t1) {
                    return t0.getSymbol().compareTo(t1.getSymbol());
                }
            });
            databaseHandler.addStock(stock);
            stockAdapter1.notifyDataSetChanged();
        }
    }

    public void updateData(ArrayList<Stock> cList){
        if (!fStocks.isEmpty()){
            fStocks.clear();
        }

        fStocks.addAll(cList);


    }

    @Override
    protected void onDestroy() {
        databaseHandler.destroy();
        super.onDestroy();
    }

    public void addNewStock(Stock stock){
        databaseHandler.addStock(stock);
        ArrayList<Stock> list = databaseHandler.loadStocks();
        stockList.clear();
        stockList.addAll(list);
        new AsyncData(this).execute(stockList);
    }

    public ArrayList<Stock> searchStocks(String stockQuery){
        ArrayList<Stock> toRet = new ArrayList<>();
        if(stockQuery.length() <= 0){
            return toRet;
        }

        for(int i =0; i < fStocks.size(); i++){
            if(fStocks.get(i).getSymbol().contains(stockQuery.toUpperCase())){
                toRet.add(fStocks.get(i));
            }
        }
        return toRet;
    }

    /*public void removeStock(int pos){ // Needs to be edited
        stockList.remove(pos);
        Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
        stockAdapter1.notifyDataSetChanged();
    }*/
}
