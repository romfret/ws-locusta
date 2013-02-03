package mmm.locusta.authentification;

import mmm.locusta.MainActivity;
import mmm.locusta.R;
import mmm.locusta.TemporarySave;
import mmm.locusta.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Authentification extends Activity {

	@Override
	public void onBackPressed() {
		finish();
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.authentification);
		
		final Button button = (Button) findViewById(R.id.valider);
		final Button createButton = (Button) findViewById(R.id.CreateButton);
		final EditText id = (EditText) findViewById(R.id.editText1);
		final EditText pswd = (EditText) findViewById(R.id.editText2);

		createButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Authentification.this,
						CreateUser.class);
				startActivity(intent);
			}
		});

		// Action sur le bouton
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				String idOnClick = "" + id.getText();// Cast Editable to String
				String pswdOnClick = "" + pswd.getText();

				Connection myConnection = new Connection(idOnClick, pswdOnClick);
				User user = myConnection.findUser();
				TextView labelError = (TextView) findViewById(R.id.error);
				if (user == null) {
					Toast.makeText(getApplicationContext(), "MAUVAIS USER",
							Toast.LENGTH_SHORT).show();
					labelError.setText("Utilisateur inconnu");

				}

				else if (user.getPass() == null) {
					Toast.makeText(getApplicationContext(), "MAUVAIS PASSWORD",
							Toast.LENGTH_SHORT).show();
					labelError.setText("Mot de passe incorrect");
				} else {
					Toast.makeText(getApplicationContext(),
							"CONNEXION REUSSIE", Toast.LENGTH_SHORT).show();
					TemporarySave.getInstance().setCurrentUser(user);

					Intent intent = new Intent(Authentification.this,
							MainActivity.class);
					startActivity(intent); // LOAD l'activity de marc

				}
				setProgressBarIndeterminateVisibility(false);
			}

		});

	}

	// //Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du
	// téléphone
	// public boolean onCreateOptionsMenu(Menu menu) {
	//
	// //Création d'un MenuInflater qui va permettre d'instancier un Menu XML en
	// un objet Menu
	// MenuInflater inflater = getMenuInflater();
	// //Instanciation du menu XML spécifier en un objet Menu
	// inflater.inflate(R.layout.menu, menu);
	//
	// //Il n'est pas possible de modifier l'icône d'entête du sous-menu via le
	// fichier XML on le fait donc en JAVA
	// menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.ic_launcher2);
	//
	// return true;
	// }
	//
	//
}