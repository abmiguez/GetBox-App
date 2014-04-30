package es.getbox.android.getboxapp;

import java.util.Timer;
import java.util.TimerTask;
 
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
 
public class SplashScreenActivity extends Activity {
 
    // Set the duration of the splash screen
    private static final long SPLASH_SCREEN_DELAY = 3000;
    private SharedPreferences mPrefs;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        mPrefs = getSharedPreferences("Splash",0);
        boolean logueado = mPrefs.getBoolean("splash",false);
        if(!logueado){
        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
        // Hide title bar   
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    
        setContentView(R.layout.splash_screen);
 
        TimerTask task = new TimerTask() {
            @Override
            public void run() { 
 
                // Start the next activity
                Intent mainIntent = new Intent().setClass(
                        SplashScreenActivity.this, GetBoxActivity.class);
                startActivity(mainIntent);
 
                // Close the activity so the user won't able to go back this
                // activity pressing Back button
                finish();
            }
        };        
        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
        }else{
        	 // Start the next activity
            Intent mainIntent = new Intent().setClass(
                    SplashScreenActivity.this, GetBoxActivity.class);
            startActivity(mainIntent);

            // Close the activity so the user won't able to go back this
            // activity pressing Back button
            finish();        
        }
    }
 
}