package es.getbox.android.getboxapp;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class GetBoxActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_getbox);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.get_box, menu);
		return true;
	}

}