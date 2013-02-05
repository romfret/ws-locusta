package mmm.locusta.addEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mmm.locusta.Event;
import mmm.locusta.EventType;
import mmm.locusta.R;
import mmm.locusta.TemporarySave;
import mmm.locusta.User;
import mmm.locusta.WebClient;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class addEventService extends Service {

	private List<Integer> et_ids = new ArrayList<Integer>();
	private WebClient wc;
	
	@Override
	public void onCreate() {
		Log.d("", "addService Created!");
		super.onCreate();

		wc = new WebClient();
		List<EventType> ets = wc.getEventTypes();
		for (EventType et : ets) {
			et_ids.add(et.getId());
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				EventType et = null;
				if (intent.getExtras().getInt("typeId") != -1)
					et = wc.getEventTypeById(intent.getExtras().getInt("typeId"));
				else {
					return;
				}

				String name = intent.getExtras().getString("name");
				
				String description = intent.getExtras().getString("description");
				
				Date now = new Date();
				User current = TemporarySave.getInstance().getCurrentUser();
				if (current == null) {
					System.err
							.println("===== > SUndifined user, application exit");
					return;
				}
				Event e = new Event(name, description, now, current.getLongitude(), current.getLatitude(), current);
				e.setEventType(et);
				wc.addEvent(e);		
			}
		});
		t.start();
		return super.onStartCommand(intent, flags, startId);
	}

}