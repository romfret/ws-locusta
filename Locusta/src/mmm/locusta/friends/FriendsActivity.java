package mmm.locusta.friends;

import java.util.List;

import mmm.locusta.R;
import mmm.locusta.TemporarySave;
import mmm.locusta.User;
import mmm.locusta.WebClient;
import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class FriendsActivity extends Activity {
	protected Activity self;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.friends);
		self = this;

		final ListView lvFriends = (ListView) findViewById(R.id.yourFriends);
		final User user = TemporarySave.getInstance().getCurrentUser();
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_multiple_choice);
		lvFriends.setAdapter(arrayAdapter);
		for (User u : user.getFriends()) {
			arrayAdapter.add(u.getUserName());
		}
		for(int i = 0; i < lvFriends.getCount(); ++i)
			lvFriends.setItemChecked(i, true);

		final ListView lvOthers = (ListView) findViewById(R.id.othersFriends);
		final WebClient wc = new WebClient();
		List<User> others = wc.searchUsers("");
		final ArrayAdapter<String> arrayAdapter2 = new ArrayAdapter<String>(
				this, android.R.layout.simple_list_item_multiple_choice);
		lvOthers.setAdapter(arrayAdapter2);
		for (User o : others) {
			boolean alreadyFriend = false;
			for (User u : user.getFriends()) {
				if (u.getUserName().equals(o.getUserName())) {
					alreadyFriend = true;
					break;
				}
			}
			if (!alreadyFriend && !user.getUserName().equals(o.getUserName()))
				arrayAdapter2.add(o.getUserName());
		}

		Button button = (Button) findViewById(R.id.buttonQuit);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setProgressBarIndeterminateVisibility(true);
				user.getFriends().clear();
				SparseBooleanArray checked = lvFriends
						.getCheckedItemPositions();
				for (int i = 0; i < checked.size(); i++) {
					int position = checked.keyAt(i);
					if (checked.valueAt(i)) {
						String userName = arrayAdapter.getItem(position);
						User friend = wc.getUserByUserName(userName);
						user.getFriends().add(friend);
						
					}
				}
				

				checked = lvOthers.getCheckedItemPositions();
				for (int i = 0; i < checked.size(); i++) {
					int position = checked.keyAt(i);
					if (checked.valueAt(i)) {
						String userName = arrayAdapter2.getItem(position);
						User friend = wc.getUserByUserName(userName);
						user.getFriends().add(friend);
					}
				}
				TemporarySave.getInstance().setCurrentUser(user, true);
				setProgressBarIndeterminateVisibility(false);
				self.finish();

			}
		});

	}

}
