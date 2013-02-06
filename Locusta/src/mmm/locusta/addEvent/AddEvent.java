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
import android.util.Log;

public class AddEvent {

	private List<Integer> et_ids = new ArrayList<Integer>();
	private WebClient wc;

	public AddEvent(){
		System.out.println("addService created");

		wc = new WebClient();
		List<EventType> ets = wc.getEventTypes();
		for (EventType et : ets) {
			et_ids.add(et.getId());
		}

	}
	
	public void execute(String name, int typeId, String description) {
		System.out.println("addService running");
		EventType et = null;
		if (typeId != -1)
			et = wc.getEventTypeById(typeId);
		else {
			et = wc.getEventTypeById(123); // default type
		}

		if(name == null){
			System.out.println("name null");
		}

		if(description == null){
			System.out.println("description null");
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
		int id = e.getId();
		System.out.println("id="+id);
		System.out.println(wc.getEventById(id).getName());
	}

}