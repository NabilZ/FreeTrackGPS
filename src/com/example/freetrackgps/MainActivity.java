package com.example.freetrackgps;

import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.location.*;
import android.content.*;
import android.view.MenuItem;
import android.app.AlertDialog;

import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	private LocationManager service;
	private TextView gpsStatus, gpsPosition, workoutStatus, workoutDistance, workoutSpeed;
	private String provider;
	private List<TextView> textViewElements;
	private Button pauseButton, startButton;
    private SharedPreferences sharedPrefs;
	private RouteManager currentRoute;
    private GPSConnectionManager gpsConnect;
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = getSharedPreferences("Pref", Activity.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
        gpsConnect = new GPSConnectionManager(this);
		setContentView(R.layout.activity_main);
		service = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		gpsStatus = (TextView)this.findViewById(R.id.textGPSStatus);
		gpsPosition = (TextView)this.findViewById(R.id.textPosition);
		workoutStatus = (TextView)this.findViewById(R.id.textSatlites);
		workoutDistance = (TextView)this.findViewById(R.id.textWorkOut);
        workoutSpeed = (TextView)this.findViewById(R.id.textSpeedView);
		textViewElements = Arrays.asList(gpsPosition, gpsStatus, workoutDistance, workoutSpeed);
		pauseButton = (Button)this.findViewById(R.id.pauseButton);
		startButton = (Button)this.findViewById(R.id.startButton);
		currentRoute = new RouteManager(this);
		startButton.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View V){ onStartRoute();}
		});
		pauseButton.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View V){ onPauseRoute();}
		});

        gpsConnect.onCreateConnection(textViewElements, service, currentRoute);
        if(currentRoute.getStatus()!= DefaultValues.routeStatus.start)
            setPreviewStatus(View.INVISIBLE);
        else
            setPreviewStatus(View.VISIBLE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        if (currentRoute.getStatus() != DefaultValues.routeStatus.start) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return  false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_workout:
                Intent ine = new Intent(this, WorkoutViewer.class);
                startActivity(ine);
                break;
            case R.id.action_settings:
                Intent inent = new Intent(this, Settings.class);
                startActivity(inent);
                break;
            case R.id.gpsSetting:
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.action_about:
                Intent inet = new Intent(this, About.class);
                startActivity(inet);
            break;
        }
        return true;
    }

    public void setPreviewStatus(int status){
        if (sharedPrefs.getBoolean("showWorkoutInfo", false)==false) {
            workoutDistance.setVisibility(status);
            workoutStatus.setVisibility(status);
            workoutSpeed.setVisibility(status);
            ((TextView) this.findViewById(R.id.textView4)).setVisibility(status);
            ((TextView) this.findViewById(R.id.textViewVersion)).setVisibility(status);
            ((TextView) this.findViewById(R.id.textView)).setVisibility(status);
        }
    }
	public void onStartRoute() {
		if (currentRoute.getStatus() == DefaultValues.routeStatus.stop && gpsConnect.getStatus() == true){
			currentRoute.start();
			workoutStatus.setText(getString(R.string.activeLabel));
			startButton.setText(getString(R.string.stopLabel));
            setPreviewStatus(View.VISIBLE);
		}
		else {
            if (gpsConnect.getStatus() == true && currentRoute.getStatus() != DefaultValues.routeStatus.stop ) {
                currentRoute.stop();
                workoutStatus.setText("--");
                startButton.setText(getString(R.string.startLabel));
                setPreviewStatus(View.INVISIBLE);
            }
            else{
                Toast.makeText(getBaseContext(), getString(R.string.errorGPSConnectuionInfo), Toast.LENGTH_LONG).show();
            }
        }
	}
	public void onPauseRoute(){
			if(currentRoute.getStatus() == DefaultValues.routeStatus.start){
				currentRoute.pause();
				workoutStatus.setText(getString(R.string.pauseLabel));
				pauseButton.setText(getString(R.string.unPauseLabel));
			}
			else if (currentRoute.getStatus() == DefaultValues.routeStatus.pause){
				currentRoute.unpause();
				workoutStatus.setText(getString(R.string.activeLabel));
				pauseButton.setText(getString(R.string.pauseLabel));
			}
	}
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.finishApp))
                .setMessage(getString(R.string.finishAppInfo))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        if (currentRoute.getStatus() != DefaultValues.routeStatus.stop)
                            currentRoute.stop();
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    protected void onDestroy(){
        super.onDestroy();
        if (currentRoute.getStatus() != DefaultValues.routeStatus.stop)
            currentRoute.stop();
    }
}