package mmm.locusta.utils;

import java.util.Timer;
import java.util.TimerTask;

import mmm.locusta.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

public class Splash extends Activity {

	private Timer timer = new Timer();
	private TimerTask task;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		periodicallyActivate(5000);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	


	public void periodicallyActivate(double perdiodInMiliSeconds) {
		task = new TimerTask() {
			public void run() {
				Splash.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						stopSplash();
					}
				});
			}
		};
		timer.schedule(task, 0, (long) perdiodInMiliSeconds);
	}
	
	
	public void stopSplash() {
		task.cancel();
		task = null;
		timer = null;
		System.exit(0);
	}

}
