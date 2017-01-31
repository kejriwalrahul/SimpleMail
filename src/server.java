import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class CommandParser{
	String userid = "";
	// Change it to DataInputStream	
	File curfile = null;
	
	void errorExit(String s){
		System.err.println("Error: " + s);
		System.exit(1);
	}
	
	String listUsers(){
		String res = "";
		File dir = new File(".");
		File[] dirFiles = dir.listFiles();
		
		for(File fil: dirFiles){
			if(fil.isFile() && fil.getName().contains(".dat"))
				res += fil.getName().substring(0, fil.getName().indexOf(".dat")) + " ";
		}
		
		return res;
	}
	
	String addUser(String u){
		File f = new File("./" + u + ".dat");
		
		// If file already exists, send msg
		if(f.exists() && !f.isDirectory())
			return "Userid already present";
			
		try{
			boolean res = f.createNewFile();	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return "";
	}
	
	String setCurrUser(String u){
		String res = "";
		
		if(userid != "")
			closeUser();
		
		File f = new File("./" + u + ".dat");
		if(!f.exists())
			return "User does not exist!";
		
		userid = u;
		
		
		
		return res;
	}
	
	String readMsg(){
		String res = "";
		
		return res;
	}
	
	String delMsg(){
		String res = "";
		
		return res;
	}
	
	String sendMsg(String u){
		String res = "";
		
		return res;
	}
	
	String closeUser(){
		String res = "";
		userid =  "";
//		curfile.
		
		
		return res;
	}
	
	String closeConnection(){
		String res = "";
		
		return res;
	}
	
	String dispatchCommand(String cmd){
		String[] tokens = cmd.split(" ");
		
		if(tokens[0] == "LSTU")
			return listUsers();
		else if(tokens[0] == "ADDU")
			return addUser(tokens[1]);
		else if(tokens[0] == "USER")
			return setCurrUser(tokens[1]);
		else if(tokens[0] == "READM")
			return readMsg();
		else if(tokens[0] == "DELM")
			return delMsg();
		else if(tokens[0] == "SEND")
			return sendMsg(tokens[1]);
		else if(tokens[0] == "DONEU")
			return closeUser();
		else if(tokens[0] == "QUIT")
			return closeConnection();
		else
			errorExit("Unknown Command!");
	
		// To satisfy compiler
		return "";
	}
}

/*
 * To do:
 * 		Fork thread for each client and handle
 * 		Fix socket read via looping
 */
class Server implements Runnable{
	ServerSocket sock;
	Socket usersock;
	Thread t;
	
	Server(int port) throws IOException{
		sock = new ServerSocket(port, 5);
	}

	/*
	 * Accepts connections from users and handles one at a time
	 */
	public void run(){
		try{	
//			 CommandParser c = new CommandParser();
			while(true){
				DataInputStream in = new DataInputStream(usersock.getInputStream());
				String cmd = in.readUTF();
				System.out.println("Recvd: " + cmd);
				
				// String res = c.dispatchCommand(cmd);
				if(cmd.equals("QUIT")){
					System.out.println("Exiting!");
					break;
				}
					
				// Send back res
				DataOutputStream out = new DataOutputStream(usersock.getOutputStream());
				out.writeUTF("This is my response to you");
			}
			usersock.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}	
	
	public void start(){
		while(true){
			try{
				usersock = sock.accept();
				t = new Thread(this, "user thread " + Integer.toString(usersock.getLocalPort()));
				t.run();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
}

public class server{
	public static void main(String[] args) throws IOException{
		CommandParser c = new CommandParser();
		
		int port = Integer.parseInt(args[0]);
		
		try{
			Server s = new Server(port);
			s.start();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}