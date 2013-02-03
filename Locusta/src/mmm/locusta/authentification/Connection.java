package mmm.locusta.authentification;

import mmm.locusta.User;
import mmm.locusta.WebClient;



public class Connection{
	private String id, password, inBuff;

	public Connection(String id, String password){
		this.id = id;
		this.password = password;
		inBuff="";
	}

	public User findUser(){
		
		WebClient wc = new WebClient();
		System.out.println("Connexion etablie");

		User currentUser = null;
		
		currentUser = wc.getUserByUserName(id);
		//retourne null si l'user n'existe pas
		
		if (currentUser == null) {
			return null;
		}
					
		//pas de == avec les string
		if (currentUser.getPass().equals(wc.encryptPassword(password))) {				
			System.out.println("OK c'est le bon !");
			return currentUser;
			//Si le pasd est bon
		}
		else {
			return new User(id, null);
			//sinon
		}

	}
	
	public void saveUser(String userName, String pass) {
		WebClient wc = new WebClient();
		User user = new User(userName, wc.encryptPassword(pass));
		wc.userRegistration(user);
	}
	

}
