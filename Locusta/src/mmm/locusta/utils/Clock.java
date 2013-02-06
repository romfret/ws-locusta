package mmm.locusta.utils;

import java.util.Timer;
import java.util.TimerTask;

import mmm.locusta.MainActivity;

public class Clock {

	private Timer timer = new Timer();
	private TimerTask task;

	public void periodicallyActivate(final MainActivity activity, double perdiodInMiliSeconds) {
		task = new TimerTask() {
			public void run() {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						activity.refreshItems();
					}
				});
			}
		};
		timer.schedule(task, 0, (long) perdiodInMiliSeconds);
	}
	
	public void cancel() {
		task.cancel();
//		timer.cancel();
	}
}