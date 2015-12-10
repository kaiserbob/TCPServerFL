import java.net.Socket;

public class ClientChat {
	private String addr_ip;
	private int tcp_port;
	private String nick;
	private int joined_id;
	private Socket socket;
	
	ClientChat(String clientName, String clientIP, int clientPort, int clientID, Socket sock){
		this.nick = clientName;
		this.addr_ip = clientIP;
		this.tcp_port = clientPort;
		this.joined_id = clientID;
		this.socket = sock;
	}
	
	public String getAddr_ip() {
		return addr_ip;
	}
	public void setAddr_ip(String addr_ip) {
		this.addr_ip = addr_ip;
	}
	public int getTcp_port() {
		return tcp_port;
	}
	public void setTcp_port(int tcp_port) {
		this.tcp_port = tcp_port;
	}
	public String getNick() {
		return nick;
	}
	public void setNick(String nick) {
		this.nick = nick;
	}
	public int getJoined_id() {
		return joined_id;
	}
	public void setJoined_id(int joined_id) {
		this.joined_id = joined_id;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
