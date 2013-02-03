package mmm.locusta.utils;

import java.util.Timer;
import java.util.TimerTask;

import mmm.locusta.EventType;
import mmm.locusta.R;
import mmm.locusta.WebClient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

public class Splash extends Activity {

	private Timer timer = new Timer();
	private TimerTask task;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private static final int STOPSPLASH = 0;
	private static final long SPLASHTIME = 5000;
	private boolean isDown = false;
	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				try {
					WebClient wc = new WebClient();
					if (wc.getEventTypes() == null)
						launchErrorActivity("Serveur down!");

				} catch (Exception e) {
					launchErrorActivity("Serveur down!");

				}
				if (!isDown) {
					Intent intent = new Intent(Splash.this,
							mmm.locusta.authentification.Authentification.class);
					startActivity(intent);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void launchErrorActivity(String message) {
		isDown = true;
		Intent intentError = new Intent(Splash.this,
				mmm.locusta.utils.ErrorActivity.class);
		intentError.putExtra("err_msg", message);
		startActivity(intentError);
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		Message msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
	}

}
