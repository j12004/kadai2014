package com.example.prog8_1;

import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity implements Runnable{
	ProgressDialog mProgressDialog;
	MyAsyncHttpClient myAsyncHttpClient;

	static String s = "";

	public String getS(){
		return s;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		myAsyncHttpClient = new MyAsyncHttpClient(getApplicationContext());
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.option_menu, menu);//menu
		 //Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item){
		super.onMenuItemSelected(featureId, item);

		switch(item.getItemId()){

		case R.id.item01:
			Random r = new Random();
			int n = r.nextInt(3000000);
			s =(String.valueOf(n));

			Intent selectIntent = new Intent(MainActivity.this, CameraActivity.class);
			startActivity(selectIntent);
		break;

		case R.id.item02:
			myAsyncHttpClient.newRequestParams();
			myAsyncHttpClient.setParams("name",s);
			myAsyncHttpClient.setParams("power",s);
			myAsyncHttpClient.setParams("photo",s);
			myAsyncHttpClient.access();
		break;

		}
	return true;
	}

	@Override
	public void run() {
		try{
			mProgressDialog.setMax(5);
			for(int i=1; i<=5; i++){
				mProgressDialog.setProgress(i);
				Thread.sleep(1*1000);
			}
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		mProgressDialog.dismiss();
		// TODO 自動生成されたメソッド・スタブ
	}
}
