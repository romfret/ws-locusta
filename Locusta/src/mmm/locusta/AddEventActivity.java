package mmm.locusta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class AddEventActivity extends Activity {

	private List<Integer> et_ids = new ArrayList<Integer>();
	private int selectedId = -1;
	private WebClient wc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.add_event);

		Spinner spinner = (Spinner) findViewById(R.id.spinnerEventType);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

		// et_ids.add(-1);
		// adapter.add("all");
		wc = new WebClient();
		List<EventType> ets = wc.getEventTypes();
		for (EventType et : ets) {
			et_ids.add(et.getId());
			adapter.add(et.getName());
		}
		if (ets.size() > 0)
			selectedId = ets.get(0).getId();

		adapter.notifyDataSetChanged();
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				selectedId = et_ids.get(pos);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public void ok(View v) {
		Button b = (Button) findViewById(R.id.okAdd);
		b.setText("Please wait...");
		b.setEnabled(false);
		final Activity self = this;
		setProgressBarIndeterminateVisibility(true);
		Thread t = new Thread(new Runnable() {
			public void run() {
				
				EventType et = null;
				if (selectedId != -1)
					et = wc.getEventTypeById(selectedId);
				else {
					return;
				}
				EditText nameV = (EditText) findViewById(R.id.txtName);
				String name = nameV.getText().toString();
				EditText descrV = (EditText) findViewById(R.id.txtDescr);
				String description = descrV.getText().toString();
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
				self.finish();
			}
		});
		t.start();
		setProgressBarIndeterminateVisibility(false);

	}
}