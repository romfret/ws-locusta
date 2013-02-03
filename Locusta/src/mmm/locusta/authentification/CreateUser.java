package mmm.locusta.authentification;

import mmm.locusta.R;
import mmm.locusta.User;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class CreateUser extends Activity {
	/** Called when the activity is first created. */
	protected Activity self;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		 requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.create_user);
		final Button button = (Button) findViewById(R.id.valider);
		final EditText id = (EditText) findViewById(R.id.editText1);
		final EditText pswd = (EditText) findViewById(R.id.editText2);
		final EditText pswd2 = (EditText) findViewById(R.id.EditText01);
		self = this;
		
	
	

		// Action sur le bouton
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				String idOnClick = "" + id.getText();// Cast Editable to String
				String pswdOnClick = "" + pswd.getText();
				String pswdOnClick2 = "" + pswd2.getText();

				Connection myConnection = new Connection(idOnClick, pswdOnClick);
				User user = myConnection.findUser();
				TextView labelError = (TextView) findViewById(R.id.error);
				if (user != null) {
					Toast.makeText(getApplicationContext(), "USER EXISTANT",
							Toast.LENGTH_SHORT).show();
					labelError.setText("Utilisateur déjà existant");

				}

				else if (pswdOnClick.equals(pswdOnClick2)) {
					Toast.makeText(getApplicationContext(),
							"PASSWORD DIFFERENTS", Toast.LENGTH_SHORT).show();
					labelError.setText("Mots de passe différents");
				} else {
					setProgressBarIndeterminateVisibility(true);
					Toast.makeText(getApplicationContext(), "UTILISATEUR CREE",
							Toast.LENGTH_SHORT).show();
					myConnection.saveUser(idOnClick, pswdOnClick2);
					setProgressBarIndeterminateVisibility(false);
					setProgressBarIndeterminateVisibility(false);
					self.finish();
					
				}

			}

		});

	}

}