import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MyServer {
	
	private ServerSocket sSocket;
	private static ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	
	public MyServer(ServerSocket srvSck){
		this.sSocket = srvSck;
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
		private MyServer server;
		private boolean killServer;
		
		public RunServer(Socket socket, MyServer server){
			this.socket = socket;
			this.server = server;
			this.killServer = false;
		}
		
		/**
		 * 
		 * @param msg :  is a String received from the client that need to be processed
		 * @return : returns a String, either he standardized answer to an HELO command, or send the client for a valid command if it's not
		 * Or process with the termination of the service to answer the KILL_SERVICE request
		 */
		public String processMsg (String msg){
			String dataSorter;
			if (msg.startsWith("HELO")){
				dataSorter = msg + "\nIP:" + socket.getLocalAddress().toString().substring(1) + "\nPort:" + sSocket.getLocalPort() + "\nStudentID:15333481";
			}
			else if(msg.equals("KILL_SERVICE")){
				killServer = true;
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				server.stopServer();
				dataSorter = null;
			}
			else{
				dataSorter = "Server can only process HELO and KILL_SERVICE typed messages.";
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
			try {
				PrintStream os = new PrintStream(socket.getOutputStream());
				InputStreamReader is = new InputStreamReader(socket.getInputStream());
	        	BufferedReader d = new BufferedReader(is, 4096);
	        	Date date = new Date();
				while(!killServer){
					request = d.readLine();
					System.out.println(date.toString() + " Server request : " + request);
					if(this.processMsg(request) == null){
						System.out.println("Service terminated by client.");
					}
					else {
						os.flush();
						os.println(this.processMsg(request));
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
