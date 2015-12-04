import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MyServer {
	
	private ServerSocket sSocket;
	private ArrayList <ChatRoom> roomsBook;
	private static ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	
	public MyServer(ServerSocket srvSck){
		this.sSocket = srvSck;
		this.roomsBook = new ArrayList <ChatRoom> ();
		ChatRoom r1 = new ChatRoom("room1");
		this.roomsBook.add(r1);
		
	}
	
	/**
	 * The method to start the server with the proper values
	 */
	public void start(){
		try{
			while(true){
				System.out.println("Waiting for connection...");
				Socket socket = sSocket.accept();
				System.out.println("Processing incoming connection...");
				MyServer.executorService.execute(new RunServer(socket, this));
			}
		}
		catch(Exception e){
			System.out.println(e);
		}
	}
	
	/**
	 * 
	 * The RunServer Class implements Runnable, it will perform the action of the Threads in the pool
	 *
	 */
	public class RunServer implements Runnable {
		
		private Socket socket;
		private String request;
		private ArrayList <String> reqTable;
		private MyServer server;
		private boolean killServer;
		
		public RunServer(Socket socket, MyServer server){
			this.socket = socket;
			this.server = server;
			this.killServer = false;
			this.reqTable= new ArrayList <String>();
		}
		
		/**
		 * Check  if the name of the room is already present in the rooms book
		 * @param c : takes a ChatRoom object as input
		 * @return : 1 if the room name is already entered in the room book, 0 if not
		 * @throws RemoteException
		 */
		public int checkName (String name) throws RemoteException 
		{
			Iterator <ChatRoom> it = roomsBook.iterator();
			while (it.hasNext())
			{
				if (name.compareTo(it.next().getName())==0)
				{
					return (1);
				}
			}
			return 0;	
		}
		
		/**
		 * To transform a string into a hash
		 * @param str : takes as input a string to hash.
		 * @return the hash value of the string as int.
		 */
		public int hashCode(String str) {
			  int hash = 0;
			  for (int i = 0; i < str.length(); i++) 
			  {
			    hash = hash * 31 + str.charAt(i);
			  }
			  return Math.abs(hash);
		}
		
		/**
		 * Method to obtain the ref to a chat room form its name
		 * @param name : takes a String in input which is the name of the chat room we want to get the ref for.
		 * @return the ref of the room as an integer.
		 */
		public int getRoomRefFromName (String name) {
			int retour = 0;
			ChatRoom cr = new ChatRoom();
			Iterator <ChatRoom> it = roomsBook.iterator();
			while (it.hasNext())
			{
				if (name.compareTo((cr=it.next()).getName())==0)
				{
					retour = cr.getRoom_ref();
				}
			}
			return retour;
		}
		
		/**
		 * Use this method to obtain the object ChatRoom from a room ref.
		 * @param roomRef : Takes the integer reference of a room as input.
		 * @return The object ChatRoom of the selected room.
		 */
		public ChatRoom getChatRoomFromRef (int roomRef) {
			Iterator <ChatRoom> it = roomsBook.iterator();
			ChatRoom cr = new ChatRoom();
			while (it.hasNext())
			{
				if ( roomRef == (cr=it.next()).getRoom_ref())
				{
					return cr;
				}
			}
			return cr;
		}
		
		/**
		 * Use this method to add a user to a chat room.
		 * @param roomRef : Takes the reference of the room to join as an integer.
		 * @param cc : Takes the chat user who wishes to join the specified room.
		 */
		public void addUserToRoom (int roomRef, ClientChat cc) {
			ChatRoom cr = getChatRoomFromRef(roomRef);
			cr.addChatUser(cc);
		}
		
		/**
		 * Send a message to a socket.
		 * @param sck :  takes a Socket as input to send a message to the user 
		 * @param msg : the message to send
		 * @throws IOException
		 */
		public void sendMsg (Socket sck, String msg) throws IOException {
			PrintStream oss = new PrintStream(sck.getOutputStream());
			oss.flush();
			oss.println(msg);
		}
		
		/**
		 * Notify users in a chat room that a new user has joined the room
		 * @param cc : takes a new client and warn already existing users of the chat room of the newcomer
		 * @throws IOException 
		 */
		public void broadcast (ChatRoom cr, String userName, String msg) throws IOException {
			Iterator <ClientChat> it = cr.getChatUsers().iterator();
			while (it.hasNext())
			{
				sendMsg(it.next().getSocket(), msg);
			}
		}
		
		/**
		 * 
		 * @param msg : takes the incoming leaving message as input
		 * @return : returns a String containing the message to send back to the client
		 * @throws IOException 
		 */
		public String leaveChat(ChatRoom cr, ArrayList <String> msg) throws IOException {
			String retour = "";
			Iterator <ClientChat> it = cr.getChatUsers().iterator();
			retour = "LEFT_CHATROOM:" + msg.get(0).substring(15) + "\nJOIN_ID:" + msg.get(1).substring(8);
			while (it.hasNext())
			{
				if (Integer.parseInt(msg.get(1).substring(8)) == it.next().getJoined_id())
				{
					broadcast(cr, it.next().getNick(), "A client has left the room. Good bye, " + msg.get(2).substring(12));
					it.remove();
				}
			}
			return retour;
		}
		
		/**
		 * 
		 * @param type : takes the error type as input to define the description error to return
		 * @return : returns a String to notify the client with an error code and a description
		 */
		public String errorHandler (int type) {
			String retour = "";
			switch (type) {
				// The chat room cannot be found in the roomsBook
				case 1:
					retour = "ERROR_CODE: 1\nERROR_DESCRIPTION: This chat room doesn't exist.";
				// The client  is sending a wrong command 
				case 2:
					retour = "ERROR_CODE: 2\nERROR_DESCRIPTION: Server can not process this type of command.";
			}
			return retour;		
		}
		
		/**
		 * Use this method to disconnect a client from the server
		 * @param clientName : Takes an input String which is the name of the client that wishes to disconnect the server.
		 */
		public void disconnectUser (String clientName) {
			// We run through the list of chat rooms
			Iterator <ChatRoom> it = roomsBook.iterator();
			while (it.hasNext())
			{
				// For each Chat room we run the users list
				Iterator <ClientChat> ut = it.next().getChatUsers().iterator();
				while (ut.hasNext())
				{
					if (clientName == ut.next().getNick())
					{
						// if the user that wishes to disconnect is actually in the room, he send a leaving message then we remove him from the list
						try {
							broadcast(it.next(), ut.next().getNick(), "A client has left the room. Good bye, " + clientName);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ut.remove();
					}
				}
			}
			// We then close the socket
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * Use this method to send chat messages to all the clients of the chat room
		 * @param joinRequest : Takes the CHAT message as input
		 * @return : Returns a string, the original message
		 */
		public String msgDealer(ArrayList<String> msg){
			String retour = "CHAT:"+msg.get(0).substring(5)+"\nJOIN_ID:"+msg.get(2).substring(12)+"\nMESSAGE:"+msg.get(3).substring(8);
			try {
				broadcast(getChatRoomFromRef(Integer.parseInt(msg.get(0).substring(5))), msg.get(2).substring(12), retour);
			} catch (NumberFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return retour;
		}
		
		/**
		 * Use this method to determine the number of iteration of the for loop, waiting for a message coming from
		 * the client to be fully completed before sending it to the process method
		 * @param command : Takes the command sent by the client to determine how many other messages we should expect from him
		 * @return : Returns an integer to adapt the loop
		 */
		public int msgMakingLoop (String command){
			int i;
			if (command.startsWith("LEAVE") || command.startsWith("DISCONNECT")) 
			{
				i=1;
			}
			else if (command.startsWith("JOIN") || command.startsWith("CHAT")) 
			{
				i=0;
			}
			else
				i=5;
			return i;
		}
		
		/**
		 * Use this method to append data to the ArrayList of the request before sending it to be processed
		 * @param request Takes as input the incoming data from client and adds it to the ArrayList
		 */
		public void addReqTable(String request) {
			this.reqTable.add(request);
		}
		
		/**
		 * 
		 * @param msg :  is an ArrayList of Strings received from the client that need to be processed
		 * @return : returns a String, either the standardized answer to a valid (or not) command, 
		 * Or process with the termination of the service to answer the KILL_SERVICE request
		 * @throws IOException 
		 */
		public String processMsg (ArrayList<String> msg) throws IOException{
			String dataSorter, clientIP, roomName, clientName;
			int clientPort, clientID, roomRef;
			ClientChat cc;
			
			if (msg.get(0).startsWith("HELO")){
				dataSorter = msg.get(0) + "\nIP:" + socket.getLocalAddress().toString().substring(1) + "\nPort:" + sSocket.getLocalPort() + "\nStudentID:15333481";
			}
			else if(msg.get(0).startsWith("KILL_SERVICE")){
				killServer = true;
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				server.stopServer();
				dataSorter = null;
			}
			else if(msg.get(0).startsWith("JOIN_CHATROOM")){
				roomName = msg.get(0).substring(14);
				clientIP = socket.getInetAddress().getHostAddress();
				clientPort = socket.getPort();
				clientName = msg.get(3).substring(12);
				
				// Check if the room exists in the book, if yes, add the new client to the clients list 
				// and warn the others
				if (checkName(roomName) == 1 )
				{
					clientID = hashCode(clientName);
					roomRef = getRoomRefFromName(roomName);
					cc = new ClientChat(clientName, clientIP, clientPort, clientID, socket);
					addUserToRoom(roomRef, cc);
					dataSorter = "JOINED_CHATROOM:" + roomName + "\nSERVER_IP:" + sSocket.getInetAddress().getHostAddress() + "\nPORT:"+sSocket.getLocalPort() + "\nROOM_REF:" + roomRef +"\nJOIN_ID:" + clientID;
				}
				else
				{
					dataSorter = errorHandler(1);				
				}
			}
			else if (msg.get(0).startsWith("CHAT")){
				dataSorter = msgDealer(msg);
			}
			else if (msg.get(0).startsWith("LEAVE_CHATROOM")){
				roomRef = Integer.parseInt(msg.get(0).substring(15));
				dataSorter = leaveChat(getChatRoomFromRef(roomRef), msg);
			}
			else if (msg.get(0).startsWith("DISCONNECT")){
				disconnectUser(msg.get(2).substring(12));
				dataSorter = null;
			}
			
			else{
				dataSorter = errorHandler(2);
			}
			
			return dataSorter;
			
		}
		
		/**
		 * @param No input parameters, nor output
		 * @throws The getInputStream()/getOutputStream() method can throw an IO Exception
		 * The method instantiates the Input and Output Streams to allow data exchange on the TCP socket
		 * It then calls the processMsg method to treat the incoming data
		 * If a message has to be sent back, it write it back to the client
		 */
		public void run() {
			int i;
			try {
				PrintStream os = new PrintStream(socket.getOutputStream());
				InputStreamReader is = new InputStreamReader(socket.getInputStream());
	        	BufferedReader d = new BufferedReader(is, 4096);
	        	Date date = new Date();
				while(!killServer){
					request = d.readLine();
					System.out.println(date.toString() + " Server request : " + request);
					addReqTable(request);
					for (i=msgMakingLoop(request);i<5;i++)
					{
						request = d.readLine();
						addReqTable(request);
						i++;
					}
					if(this.processMsg(reqTable) == null){
						System.out.println("Service terminated by client.");
					}
					else {
						os.flush();
						os.println(this.processMsg(reqTable));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/** Terminating the service
	 *  @param The method takes no input parameter and return no output
	 *  We first shutdown immediately all the threads in the pool
	 *  We then close the TCP server socket
	 *  We exit the program
	*/	
	public void stopServer(){
		try {
			executorService.shutdownNow();
			sSocket.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param args : takes one input integer, the TCP port for the server to listen to 
	 * @throws IOException
	 * The method check that at least one valid parameter is given as input
	 * It then instantiate a new MyServer
	 * It then runs the server
	 */
	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
    	    try {
    	    	int srvPort = Integer.parseInt(args[0]);
    	    	System.out.print("Server will listen on port : " + srvPort + "\n");
    	    	MyServer server = new MyServer(new ServerSocket(srvPort));
    			System.out.println("Starting Server...");
    			server.start();
    			System.out.println("Listening...");
    	    } catch (NumberFormatException e) {
    	        System.err.println("Argument" + args[0] + " must be an integer.");
    	    }
    	}
    	else
    	{
    		System.err.println("Error : You should specify port number.");
    	}
	}
	
}
