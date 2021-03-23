package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    protected TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);
        ProgressBar pgb1 = (ProgressBar)findViewById(R.id.progressbar1);
        text1 = (TextView)findViewById(R.id.text1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "reset", Toast.LENGTH_SHORT).show();
                pgb1.setProgress(0);
                if (pgb1.getVisibility() == View.INVISIBLE || pgb1.getVisibility() == View.GONE){
                    pgb1.setVisibility(View.VISIBLE);
                    text1.setText(null);
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"2",Toast.LENGTH_SHORT).show();
                int progress = pgb1.getProgress();
                progress += 10;
                pgb1.setProgress(progress);
                if(progress == pgb1.getMax()){
                    Toast.makeText(MainActivity.this, "max", Toast.LENGTH_SHORT).show();
                    if (pgb1.getVisibility() == View.VISIBLE){
                        pgb1.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        //Log.e("btn1", "onCreate execute");
    }


    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.one_item:
                //Toast.makeText(this, "add", Toast.LENGTH_SHORT).show();
                Intent it =new Intent(this, SecondActivity.class);
                it.putExtra("in_data", "hello 2");
                startActivityForResult(it, 2);
                break;
            case R.id.two_item:
                //Toast.makeText(this, "remove", Toast.LENGTH_SHORT).show();
                Intent it2 = new Intent("com.example.activity.ACTION_START");
                it2.addCategory("com.example.activity.MY_CATEGORY");
                it2.putExtra("in_data", "hello 2");
                startActivityForResult(it2, 1);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
            case 2:
                if (resultCode == RESULT_OK){
                    String returnedData = data.getStringExtra("data_return");
                    Log.d("FirstActivity", returnedData);
                    text1.setText(returnedData);
                }
                break;
            default:
        }
    }

}
