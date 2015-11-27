import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {
   
	private static boolean clientRun = true;
	
	private static class Reception implements Runnable {

		private InputStreamReader is;
		private Socket socket;
		private String responseLine = null;
	    private BufferedReader d;
		
		public Reception(Socket socket) throws IOException{
			this.socket = socket;
			this.is =  new InputStreamReader(socket.getInputStream());
			this.d =  new BufferedReader(is, 4096);
		}
		
		public void run() {
			while (clientRun)
			{
				try {
					responseLine = d.readLine();
					System.out.println(" Server sent : " + responseLine);
            	} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	private static class Emission implements Runnable {

		private PrintStream os;
		private String  message = null;
		private Scanner sc = null;
		
		
		public Emission(Socket socket) throws IOException {
			sc = new Scanner(System.in);
			this.os = new PrintStream(socket.getOutputStream());
		}
		public void run() {
			while(clientRun){
				  System.out.println("Please enter a message for the server :");
				  message = sc.nextLine();
				  os.println(message);
				  if (message.equalsIgnoreCase("KILL_SERVICE")){
					  clientRun = false;
				  }
				  
			  }
		}
	}
	
	
	public static void main(String[] args) {
		Socket socket;
		Thread t1, t2;
		
		try {
			socket = new Socket(InetAddress.getLocalHost(),2000);
			System.out.println(InetAddress.getLocalHost());
			System.out.println(socket.toString());
			t2= new Thread(new Emission(socket));
			t2.start();
			t1 = new Thread(new Reception(socket));
			t1.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
