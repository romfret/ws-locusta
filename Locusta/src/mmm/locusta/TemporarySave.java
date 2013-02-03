package mmm.locusta;


public class TemporarySave {
	private static TemporarySave instance = null;
	private User currentUser; // TODO à supr lors de l'jout de l apartie de dany
	private WebClient wc;
	
	public TemporarySave() {
		wc = new WebClient();
	}

	public static TemporarySave getInstance() {
		if (instance == null)
			instance = new TemporarySave();
		return instance;
	}

	public User getCurrentUser() {
		if (currentUser == null)
			return null;
		currentUser = wc.getUserById(currentUser.getId());
		return currentUser;
	}

	public void setCurrentUser(User user) {
		setCurrentUser(user, false);
	}
	public void setCurrentUser(User user, boolean refreshOnServeur) {
		currentUser = user;
		if (refreshOnServeur)
			wc.updateUser(currentUser);
	}

	
}
