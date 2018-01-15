package com.work.golinko.shkafbonuses;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TextView txtResult;
    Button mAddBonusesButton;
    Button mMinusBonusesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAddBonusesButton = findViewById(R.id.add_bonuses);
        mMinusBonusesButton = findViewById(R.id.minus_bonuses);
        txtResult = findViewById(R.id.txtResult);

        mAddBonusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddBonusesActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        mMinusBonusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MinusBonusesActivity.class);
                startActivityForResult(intent, 2);
            }
        });
    }

    //    here we check and show result of our child activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    String result = data.getStringExtra("resultAdd");
                    int numberOfBonuses = (int) Math.round(Double.parseDouble(result) / 10);
                    Toast.makeText(getApplicationContext(), "Бонусов зачислено: " + numberOfBonuses, Toast.LENGTH_LONG).show();
                } else {
                    String result = data.getStringExtra("result");
                    Toast.makeText(getApplicationContext(), "" + result, Toast.LENGTH_LONG).show();

                }
            }
            if (requestCode == 2) {
                if (resultCode == Activity.RESULT_OK) {
                    String result = data.getStringExtra("result");
                    Toast.makeText(getApplicationContext(), "Бонусов списано: " + result, Toast.LENGTH_LONG).show();
                } else {
                    String result = data.getStringExtra("result");
                    Toast.makeText(getApplicationContext(), "" + result, Toast.LENGTH_LONG).show();

                }
            }
        }
    }
}
