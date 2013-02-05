package mmm.locusta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mmm.locusta.addEvent.AddEventService;
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
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MainActivity extends MapActivity implements OnInitListener {

	private MapView mapView;
	private MapController mapController;
	private List<Overlay> mapOverlays;
	private Map<Integer, MapItemizedOverlay> itemzedOverlays;

	private int radius = 1000; // The event distance in meter
	private int zoomLevel = 17; // Default map zoom
	private UserLocationOverlay userLocationOverlay;

	private User currentUser;
	private WebClient webClient;
	private Integer specificEventTypeId = -1;

	private Intent intentTTS;
	private Intent intentAddEvent;

	private Clock timer;

	private boolean isOnError;
	private int recognitionState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_locusta_map);

		recognitionState = 0;
		isOnError = false;
		setProgressBarIndeterminateVisibility(false);

		// Load the web client
		webClient = new WebClient();
		if (webClient.getUserById(1) != null) {

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
			// currentUser = TemporarySave.getInstance().getCurrentUser(); //
			// TODO restauer quand partie dany ok
			currentUser = TemporarySave.getInstance().getCurrentUser();

			if (currentUser != null) {
				System.out.println("null");
			}

			currentUser.setLatitude(userLocationOverlay.getMyLocation()
					.getLatitudeE6() / 1E6);
			currentUser.setLongitude(userLocationOverlay.getMyLocation()
					.getLongitudeE6() / 1E6);

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

			Intent intentMapSetings = new Intent(MainActivity.this,
					MapSettings.class);
			startActivity(intentMapSetings);

			break;
		case R.id.menu_clear_events:
			clearEvents();
			Toast.makeText(getApplicationContext(), "Events cleared",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_friends:

			Intent intentFriends = new Intent(MainActivity.this,
					mmm.locusta.friends.FriendsActivity.class);
			startActivity(intentFriends);
			break;
		case R.id.menu_current_location:
			GeoPoint currentLocation = userLocationOverlay.getMyLocation();

			Geocoder geo = new Geocoder(this);
			String msg = "";
			try {
				List<Address> addresses = geo.getFromLocation(
						currentLocation.getLatitudeE6() / 1E6,
						currentLocation.getLongitudeE6() / 1E6, 1);

				if (addresses != null && addresses.size() >= 1) {
					Address address = addresses.get(0);
					msg = String.format("%s, %s %s", address.getAddressLine(0),
							address.getPostalCode(), address.getLocality());
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
			Intent intentAddEvent = new Intent(MainActivity.this,
					mmm.locusta.AddEventActivity.class);
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
			itemzedOverlays.get(event.getEventType().getId()).addOverlay(
					createOverlayItem(event));
		}
	}

	public void addFriends(Collection<User> friends) {
		for (User friend : friends) {
			// event.getEventType().getId() is the ID of the item marker
			itemzedOverlays.get(88).addOverlay(createOverlayItem(friend));
		}
	}

	/**
	 * Add only one event on the map
	 * 
	 * @param event
	 */
	public void addEvent(Event event) {
		itemzedOverlays.get(event.getEventType().getId()).addOverlay(
				createOverlayItem(event));
	}

	public void addFriend(User friend) {
		itemzedOverlays.get(88).addOverlay(createOverlayItem(friend));
	}

	/**
	 * Create an overlay item contain location and descriptions about an event
	 * 
	 * @param event
	 *            : contain location and descriptions
	 * @return
	 */
	private OverlayItem createOverlayItem(Event event) {
		GeoPoint point = new GeoPoint((int) (event.getLatitude() * 1E6),
				(int) (event.getLongitude() * 1E6));
		return new OverlayItem(point, event.getName(), String.format(
				getResources().getString(R.string.event_description), event
						.getOwner().getUserName(), event.getStartDate(), event
						.getEndDate(), event.getDescription()));
	}

	private OverlayItem createOverlayItem(User user) {
		GeoPoint point = new GeoPoint((int) (user.getLatitude() * 1E6),
				(int) (user.getLongitude() * 1E6));
		return new OverlayItem(point, getResources().getString(
				R.string.friend_toast), user.getUserName());
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
		addFriends(loadFriends());
	}

	/**
	 * Load events on the server
	 * 
	 * @return event list
	 */
	private List<Event> loadEvents() {
		GeoPoint p = userLocationOverlay.getMyLocation();
		if (specificEventTypeId == -1)
			return webClient.lookEventsAround(p.getLongitudeE6() / 1E6,
					p.getLatitudeE6() / 1E6, radius);
		else {
			EventType eventType = webClient
					.getEventTypeById(specificEventTypeId);
			return webClient.lookEventsAround(p.getLongitudeE6() / 1E6,
					p.getLatitudeE6() / 1E6, radius, eventType);
		}
	}

	private Set<User> loadFriends() {
		currentUser = TemporarySave.getInstance().getCurrentUser();
		return currentUser.getFriends();
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
		TemporarySave.getInstance().setCurrentUser(currentUser, true); // refresh
																		// on
																		// bdd
	}

	public void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}

	// ---------------- Activities ----------------//

	@Override
	protected void onResume() {
		super.onResume();
		setProgressBarIndeterminateVisibility(false);
		if (isOnError)
			System.exit(0);

		webClient = new WebClient();
		if (webClient.getEventTypes() != null) {
			// Load settings and add new events
			loadSettingsActivity();

			// Add refresh vent timer
			timer.periodicallyActivate(MainActivity.this, 10000);

			// // Load settings and add new events
			// loadSettingsActivity();
			// clearEvents();
			// addEvents(loadEvents());
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
			webClient = null;
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
		Intent intentError = new Intent(MainActivity.this,
				mmm.locusta.utils.ErrorActivity.class);
		intentError.putExtra("err_msg", message);
		startActivity(intentError);
		finish();
	}

	// ---------------- Speech recognition ----------------//

	private static final int VOICE_RECOGNITION_REQUEST = 0x10101;

	// demarrage de la reconnaissance vocale
	public void speakBtnClicked(View v) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"Please speak slowly and enunciate clearly.");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST);
	}

	private String currentEventNameToAdd;
	// retour de la reconnaissance vocale
	// reconitionState = 0 : n'attend rien
	// reconitionState = 1 : "Voulez vous définir un type ?"
	// reconitionState = 2 : "Voulez vous définir une decription ?"
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (intentTTS != null)
			stopService(intentTTS);
		if (intentAddEvent != null)
			stopService(intentAddEvent);

		if (resultCode != RESULT_OK && requestCode == VOICE_RECOGNITION_REQUEST)
			return;
		if (requestCode == VOICE_RECOGNITION_REQUEST && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			intentTTS = new Intent(this.getApplicationContext(),
					TTSService.class);
			
			String phraseEntiere = ((String)matches.get(0));
			//String phraseEntiere = "ajouter restaurant";
			
			if(recognitionState==0){ // n'attendait rien
				if (phraseEntiere.startsWith("ajouter")) { // "ajouter"
					intentAddEvent = new Intent(this.getApplicationContext(),
							AddEventService.class);
					currentEventNameToAdd = phraseEntiere.substring(9);
					intentAddEvent.putExtra("name",currentEventNameToAdd ); // "ajouter <nomEvenement>"
					recognitionState = 1;
					intentTTS.putExtra("textToSay", "Voulez vous définir un type ?");
					startService(intentTTS);
					speakBtnClicked(null);
				} else if (phraseEntiere.startsWith("lister")) { // "lister"
					User u = TemporarySave.getInstance().getCurrentUser();
					List<Event> events = webClient.lookEventsAround(
							u.getLongitude(), u.getLatitude(), radius);
					String str = "Voici les évènements à proximité, ";
					for (Event e : events) {
						str = str + e.getName() + ", ";
					}
					intentTTS.putExtra("textToSay", str);
					startService(intentTTS);
				} else {
					intentTTS.putExtra("textToSay", "Je n'ai pas compris.");
					startService(intentTTS);
				}
				
			} else if(recognitionState==1) { // attend un type
				if(phraseEntiere.startsWith("oui")){
					phraseEntiere = phraseEntiere.substring(4);
					for (EventType et : webClient.getEventTypes()) {
						if (phraseEntiere.equals(et.getName())) {
							intentAddEvent.putExtra("typeId", et.getId());
						}
					}
				}
				recognitionState = 2;
				intentTTS.putExtra("textToSay", "Voulez vous définir une description ?");
				startService(intentTTS);
				speakBtnClicked(null);
			
			} else if(recognitionState==2) { // attends une description
				if(phraseEntiere.startsWith("oui")){ 
					phraseEntiere = phraseEntiere.substring(4);
					intentAddEvent.putExtra("description", phraseEntiere);
				}
				startService(intentAddEvent);
				intentTTS.putExtra("textToSay", "évènement " + currentEventNameToAdd + "ajouté.");
				startService(intentTTS);
			}
		}
	}

	@Override
	public void onInit(int arg0) {
	}
}
