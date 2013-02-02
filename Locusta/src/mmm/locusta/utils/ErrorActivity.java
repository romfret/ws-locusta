package mmm.locusta.utils;

import mmm.locusta.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error_activity);
		
		
		String message = (String) this.getIntent().getExtras().get("err_msg");
		((TextView) findViewById(R.id.error_message)).setText(message);
		
		
		final Button exitButton = (Button) findViewById(R.id.button_exit);
		exitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				System.exit(0);
			}
		});
	}
}
