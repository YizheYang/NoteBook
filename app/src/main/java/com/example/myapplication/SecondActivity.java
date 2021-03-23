package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_second);
		Button btn3 = (Button)findViewById(R.id.button3);
		TextView text2 = (TextView)findViewById(R.id.text2);
		Intent indata = getIntent();
		String getdata = indata.getStringExtra("in_data");
		Log.d("SecondActivity", getdata);
		text2.setText(getdata);
		btn3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent it3 = new Intent(Intent.ACTION_VIEW);
//				it3.setData(Uri.parse("http://www.baidu.com"));
//				startActivity(it3);
				String data = "hello";
				Intent it4 = new Intent();
				it4.putExtra("data_return", data);
				setResult(RESULT_OK, it4);
				finish();
			}
		});
	}
}
