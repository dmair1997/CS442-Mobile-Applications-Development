package com.example.temperatureconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView History;
    private EditText FDegrees;
    private TextView convertedDegree;
    private double value;
    private double conversion;
    //private StringBuilder sb = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null)
            Toast.makeText(this,"NULL",Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this,"NOT NULL",Toast.LENGTH_LONG).show();


        Button Convertbutton = (Button) findViewById(R.id.Convertbutton);
        Convertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                convertedDegree = (TextView) findViewById(R.id.convertedDegree);
                FDegrees = (EditText) findViewById(R.id.FDegrees);
                History = (TextView) findViewById(R.id.History);

                History.setMovementMethod(new ScrollingMovementMethod());

                value = Double.parseDouble(FDegrees.getText().toString());
                RadioButton FtoC = (RadioButton) findViewById(R.id.FtoC);
                RadioButton CtoF = (RadioButton) findViewById(R.id.CtoF);


                if(FtoC.isChecked()){
                    conversion = (value - 32.0)/1.8;
                    convertedDegree.setText(conversion + "");
                    History.append(value + " F -> " + conversion + " C \n");

                }else{
                    conversion = (value*1.8)+32;
                    convertedDegree.setText(conversion + "");
                    History.append(value + " C -> " + conversion + " F \n");
                }

            }
        });



        Button ClearButton = (Button) findViewById(R.id.ClearButton);
        ClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                History = (TextView) findViewById(R.id.History);
                convertedDegree = (TextView) findViewById(R.id.convertedDegree);
                FDegrees = (EditText) findViewById(R.id.FDegrees);

                History.setText("");
                convertedDegree.setText("");
                FDegrees.setText("");
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        //outState.putDouble("DEGREE", value);
        //outState.putDouble("CONVERSION", conversion);
        outState.putString("history", History.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //value = savedInstanceState.getDouble("DEGREE");
        //History.setText(savedInstanceState.getString("history"));
        //conversion = savedInstanceState.getDouble("CONVERSION");

    }
}
