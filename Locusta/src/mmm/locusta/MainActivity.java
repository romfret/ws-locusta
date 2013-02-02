package mmm.locusta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mmm.locusta.map.MapSettings;
import mmm.locusta.map.item.ItemizedOverlaysInitialization;
import mmm.locusta.map.item.MapItemizedOverlay;
import mmm.locusta.map.location.UserLocationOverlay;
import mmm.locusta.speech.TTSService;
import mmm.locusta.utils.Clock;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements
		OnInitListener {

	private MapView mapView;
	private MapController mapController;
	private List<Overlay> mapOverlays;
	private Map<Integer, MapItemizedOverlay> itemzedOverlays;

	private int radius = 1000; // The event distance in meter
	private int zoomLevel = 17; // Default map zoom
	private UserLocationOverlay userLocationOverlay;

	private User currentUser;
	private WebClient webCient;
	private Integer specificEventTypeId = -1;

	private Intent intentTTS;
	
	private Clock timer;
	
	private boolean isOnError;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locusta_map);
		
		isOnError = false;

		// Load the web client
		webCient = new WebClient();
		if (webCient.getUserById(1) != null) {
			
			timer = new Clock();

			// Get map service
			mapView = (MapView) findViewById(R.id.mapview);
			mapView.setBuiltInZoomControls(true);

			// Get map controller
			mapController = mapView.getController();
			mapController.setZoom(zoomLevel);

			// Add item's icons in a Map
			itemzedOverlays = new ItemizedOverlaysInitialization().init(this);

			mapOverlays = mapView.getOverlays();
			// Add items to map
			for (MapItemizedOverlay item : itemzedOverlays.values()) {
				mapOverlays.add(item);
			}

			// Add geolocation
			// Add user position item marker
			userLocationOverlay = new UserLocationOverlay(this, mapView);
			mapOverlays.add(userLocationOverlay);
			userLocationOverlay.enableMyLocation();
			
			// Default options
			loadSettingsActivity();
			// Load default events
			addEvents(loadEvents());

			// Get the current user
			// currentUser = TemporarySave.getInstance().getCurrentUser(); // TODO restauer quand partie dany ok
			currentUser = webCient.getUserById(1); // Tests
			currentUser.setLatitude(userLocationOverlay.getMyLocation().getLatitudeE6() / 1E6);
			currentUser.setLongitude(userLocationOverlay.getMyLocation().getLongitudeE6() / 1E6);
			TemporarySave.getInstance().setCurrentUser(currentUser);
		} else {
			// Server is down, locusta can't running anymore
			launchErrorActivity("Sorry, server is down");
		}


		// // test
		// Date d = new Date();
		// User u = new User("userName", "pass");
		//
		// Event rennes = new Event("La rue de la soif",
		// "De la boisson ?? foison :)", d, -1.678905f, 48.112474f, u);
		// EventType eventType = new EventType("Bars");
		// eventType.setId(37);
		// rennes.setEventType(eventType);
		//
		// Event rennesBouffe = new Event("La rue de la bouffe",
		// "De la bouffe ?? foison :)", d, -1.681255f, 48.105397f, u);
		// EventType eventType2 = new EventType("Restaurant");
		// eventType2.setId(39);
		// rennesBouffe.setEventType(eventType2);
		//
		//
		//
		// Collection<Event> events = new ArrayList<Event>();
		// events.add(rennes);
		// events.add(rennesBouffe);
		// addEvents(events);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_locusta_map, menu);
		return true;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Menu selection
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:

			Intent intentMapSetings = new Intent(MainActivity.this, MapSettings.class);
			startActivity(intentMapSetings);

			break;
		case R.id.menu_clear_events:
			clearEvents();
			Toast.makeText(getApplicationContext(), "Events cleared",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_friends:
			// TODO La partie de Dany :)

			Toast.makeText(getApplicationContext(), "La partie de Dany :)",
					Toast.LENGTH_SHORT).show();
			
			Intent intentSplash = new Intent(MainActivity.this, mmm.locusta.utils.Splash.class);
			startActivity(intentSplash);
			break;
		case R.id.menu_current_location:
			GeoPoint currentLocation = userLocationOverlay.getMyLocation();

			Geocoder geo = new Geocoder(this);
			String msg = "";
			try {
				List<Address> addresses = geo.getFromLocation(currentLocation.getLatitudeE6() / 1E6, currentLocation.getLongitudeE6() / 1E6, 1);

				if (addresses != null && addresses.size() >= 1) {
					Address address = addresses.get(0);
					msg = String.format("%s, %s %s", address.getAddressLine(0), address.getPostalCode(), address.getLocality());
				} else {
					msg = "No address was found !";
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.add_event:
			Intent intentAddEvent = new Intent(MainActivity.this, mmm.locusta.AddEventActivity.class);
			startActivity(intentAddEvent);
			break;
		default:
			System.out.println("Menu id unrecognized");
			break;
		}
		return true;
	}

	// ---------------- Event ----------------//

	/**
	 * Add events on the map
	 * 
	 * @param events
	 */
	public void addEvents(Collection<Event> events) {
		for (Event event : events) {
			// event.getEventType().getId() is the ID of the item marker
			itemzedOverlays.get(event.getEventType().getId()).addOverlay(createOverlayItem(event));
		}
	}

	/**
	 * Add only one event on the map
	 * 
	 * @param event
	 */
	public void addEvent(Event event) {
		itemzedOverlays.get(event.getEventType().getId()).addOverlay(createOverlayItem(event));
	}

	/**
	 * Create an overlay item contain location and descriptions about an event
	 * 
	 * @param event
	 *            : contain location and descriptions
	 * @return
	 */
	private OverlayItem createOverlayItem(Event event) {
		GeoPoint point = new GeoPoint((int) (event.getLatitude() * 1E6), (int) (event.getLongitude() * 1E6));
		return new OverlayItem(point, event.getName(), String.format(getResources().getString(R.string.event_description),
				event.getOwner().getUserName(), event.getStartDate(), event.getEndDate(), event.getDescription()));
	}

	/**
	 * clear the event on the map
	 */
	public void clearEvents() {
		for (MapItemizedOverlay item : itemzedOverlays.values()) {
			item.clearOverlays();
		}
	}
	
	/**
	 * Refresh event list
	 */
	public void refreshEvents() {
		showToast("Refresh");
		clearEvents();
		addEvents(loadEvents());
	}

	/**
	 * Load events on the server
	 * 
	 * @return event list
	 */
	private List<Event> loadEvents() {
		GeoPoint p = userLocationOverlay.getMyLocation();
		if (specificEventTypeId == -1)
			return webCient.lookEventsAround(p.getLongitudeE6() / 1E6,
					p.getLatitudeE6() / 1E6, radius);
		else {
			EventType eventType = webCient
					.getEventTypeById(specificEventTypeId);
			return webCient.lookEventsAround(p.getLongitudeE6() / 1E6,
					p.getLatitudeE6() / 1E6, radius, eventType);
		}
	}

	// ----------- Location ------------------//

	/**
	 * Map position refresh
	 * 
	 * @param p
	 */
	public void onLocationChanged(GeoPoint p) {
		// Center the map on user
		mapController.animateTo(p);
		mapController.setCenter(p);

		// Set the current user location
		currentUser.setLatitude(p.getLatitudeE6() / 1E6);
		currentUser.setLongitude(p.getLongitudeE6() / 1E6);
	}

	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}

	// ---------------- Activities ----------------//

	@Override
	protected void onResume() {
		super.onResume();
		
		if (isOnError)
			System.exit(0);
		
		webCient = new WebClient();
		if (webCient.getUserById(1) != null) {
			// Load settings and add new events
			loadSettingsActivity();
			
			// Add refresh vent timer
			timer.periodicallyActivate(MainActivity.this, 10000);

//			// Load settings and add new events
//			loadSettingsActivity();
//			clearEvents();
//			addEvents(loadEvents());
		} else {
			// Server is down, locusta can't running anymore
			launchErrorActivity("Sorry, server is down");
		}
		

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isOnError) {
			timer.cancel();
			webCient = null;
		}
	}

	private void loadSettingsActivity() {
		SharedPreferences sp = getSharedPreferences("locusta_settings",
				Activity.MODE_WORLD_WRITEABLE);
		radius = sp.getInt("radius", 100);
		specificEventTypeId = sp.getInt("idEventType", -1);
	}
	
	public void launchErrorActivity(String message) {
		isOnError = true;
		Intent intentError = new Intent(MainActivity.this, mmm.locusta.utils.ErrorActivity.class);
		intentError.putExtra("err_msg", "Sorry, server is down.");
		startActivity(intentError);
	}

	// ---------------- Speech recognition ----------------//

	private static final int VOICE_RECOGNITION_REQUEST = 0x10101;

	public void speakBtnClicked(View v) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Please speak slowly and enunciate clearly.");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (intentTTS != null)
			stopService(intentTTS);
		if (resultCode != RESULT_OK && requestCode == VOICE_RECOGNITION_REQUEST)
			return;
		if (requestCode == VOICE_RECOGNITION_REQUEST && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			System.out.println("Voici les matches");
			for (String match : matches) {
				System.out.println(match);
			}

			// String firstMatch = matches.get(0);
			// String titre = matches.get(1);
			// String match3 = matches.get(2); // type
			// String type = matches.get(3);
			//
			// if(firstMatch.equals("ajouter")){
			// Event event = new Event(titre,)
			// event.setEventType(type);
			// webCient.addEvent(event)
			// }

			System.out.println(matches.get(0));

			intentTTS = new Intent(this.getApplicationContext(),
					TTSService.class);
			intentTTS.putExtra("textToSay", matches.get(0));
			startService(intentTTS);

		}
	}

	@Override
	public void onInit(int arg0) {
	}
}
