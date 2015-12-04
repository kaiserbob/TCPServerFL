import java.util.ArrayList;

public class ChatRoom {
	private int room_ref;
	private String name;
	private ArrayList <ClientChat> chatUsers;
	
	public ChatRoom (String name) {
		this.name = name;
		this.room_ref = hashCode(name);
		this.chatUsers = new ArrayList<ClientChat>();
	}
	
	public ChatRoom () {
		this.name = "";
		this.room_ref = 0;
		this.chatUsers = new ArrayList<ClientChat>();
	}
	
	public int hashCode(String str) {
		  int hash = 0;
		  for (int i = 0; i < str.length(); i++) 
		  {
		    hash = hash * 31 + str.charAt(i);
		  }
		  return Math.abs(hash);
	}
	
	public int getRoom_ref() {
		return room_ref;
	}
	public void setRoom_ref(int room_id) {
		this.room_ref = room_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ClientChat> getChatUsers() {
		return chatUsers;
	}

	public void setChatUsers(ArrayList<ClientChat> chatUsers) {
		this.chatUsers = chatUsers;
	}
	
	public void addChatUser (ClientChat cc) {
		this.chatUsers.add(cc);
	}
	
	public void delChatUser (ClientChat cc) {
		int indx;
		indx = this.chatUsers.indexOf(cc);
		this.chatUsers.remove(indx);
	}
}
