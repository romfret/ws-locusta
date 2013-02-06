package mmm.locusta.addEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mmm.locusta.Event;
import mmm.locusta.EventType;
import mmm.locusta.TemporarySave;
import mmm.locusta.User;
import mmm.locusta.WebClient;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AddEventService extends Service {

	private List<Integer> et_ids = new ArrayList<Integer>();
	private WebClient wc;

	@Override
	public void onCreate() {
		wc = new WebClient();
		List<EventType> ets = wc.getEventTypes();
		for (EventType et : ets) {
			et_ids.add(et.getId());
		}
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(wc ==null)
			wc = new WebClient();
		System.out.println("addService running");
		EventType et = null;
		int typeId = intent.getIntExtra("typeId", 0);
		String description = intent.getStringExtra("description");
		String name = intent.getStringExtra("name");

		et = wc.getEventTypeById(typeId);
		if (et == null)
			et = wc.getEventTypeById(123); // default type

		if (description == null) {
			description = "";
		}

		Date now = new Date();
		User current = TemporarySave.getInstance().getCurrentUser();
		if (current == null) {
			System.err.println("===== > Undifined user, application exit");
		}
		Event e = new Event(name, description, now, current.getLongitude(),
				current.getLatitude(), current);
		e.setEventType(et);
		wc.addEvent(e);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}