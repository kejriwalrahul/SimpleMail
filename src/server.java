import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To do:
 * 		Loop while receiving packets
 */

class CommandParser{
	
	// Current userid
	String userid = "";

	//	Current User File
	RandomAccessFile fil;
	// BufferedReader rfil;
	// BufferedWriter wfil;
	
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
		
		return "Add User Successful";
	}
	
	int countMsgs(){
		String line;
		int count = 0;
		
		try{
			while((line = fil.readUTF()) != null){
				if(line.trim().contains("###"))
					count++;
			}
		}
		catch(EOFException e){
			try{
				fil.seek(0);	
			}
			catch(IOException ef){
				ef.printStackTrace();
			}
		}
		catch(IOException e){
			e.printStackTrace();
			errorExit("IOError from count msgs!");
		}
		
		System.out.println("No of msgs: " + Integer.toString(count) + "\n");
		return count;
	}
	
	String setCurrUser(String u){
		String res = "";
		
		if(userid != "")
			closeUser();
		
		File f = new File("./" + u + ".dat");
		if(!f.exists())
			return "User does not exist!";
		
		userid = u;

		try{
			/*
				Path p = FileSystems.getDefault().getPath("", u+".dat");
				rfil = Files.newBufferedReader(p);
				wfil = Files.newBufferedWriter(p);
			*/
			
			fil = new RandomAccessFile(f, "rw");
		}
		catch(IOException e){
			e.printStackTrace();
			errorExit("File Open Error!");
		}
		
		return "User " + u + " exists and has " + Integer.toString(countMsgs()) + " number of messages in his/her spool file";
	}
	
	String readMsg(){
		String res = "";
		
		String s = "\n";
		int idx;
		try{
			while((s = fil.readUTF()) != null){
				if(s.trim().contains("###")){
					res += s;
					break;
				}
				res += s;
			}
		}
		catch(EOFException e){
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		if(res == "")
			res = "No More Mail";
		
		return res;
	}
	
	String delMsg(){
		String res = "";
		
		return res;
	}
	
	/*
	 * What if sending urself?
	 * 
	*/
	String sendMsg(String cmd){
		String[] parts = cmd.split("###");
		String[] toks  = parts[0].split(" ");
		
		String recvr = toks[1];

		String subj = "";
		for(int i=2;i<toks.length;i++)
			subj += toks[i] + " ";
		subj = subj.trim();
		
		String msg  = parts[1] + "\n###";
		
		File recvf = new File("./" + recvr + ".dat");
		if(!recvf.exists())
			return "Reciever does not exist!";
		
		try{
			RandomAccessFile recvfil = new RandomAccessFile(recvf, "rw");
			
			recvfil.seek(recvfil.length());
			recvfil.writeUTF("\nFrom: " + userid.trim() + "\n");
			recvfil.writeUTF("To: " + recvr.trim() + "\n");
			recvfil.writeUTF("Subject: " + subj.trim() + "\n");
			recvfil.writeUTF(msg.trim() + "\n");
			
			recvfil.close();
			
			return "Message Sent";
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return "Error";
	}
	
	String closeUser(){
		String res = "Close Successful";
		
		userid =  "";
		try{
			fil.close();
			// rfil.close();
			// wfil.close();			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return res;
	}
	
	String dispatchCommand(String cmd){
		String[] tokens = cmd.split(" ");
		
		if(tokens.length >= 2){
			if(tokens[0].equals("ADDU"))			return addUser(tokens[1]);
			else if(tokens[0].equals("USER"))		return setCurrUser(tokens[1]);
			else if(tokens[0].equals("SEND"))		return sendMsg(cmd);
			else 									return "Syntax Error!";
		}
		else{
			if(tokens[0].equals("LSTU"))			return listUsers();
			else if(tokens[0].equals("READM"))		return readMsg();
			else if(tokens[0].equals("DELM"))		return delMsg();
			else if(tokens[0].equals("DONEU"))		return closeUser();
			else if(tokens[0].equals("QUIT"))		return "";
			else 									return "Syntax Error";
		}
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
			CommandParser c = new CommandParser();
			while(true){
				DataInputStream in = new DataInputStream(usersock.getInputStream());
				String cmd;
				
				try{
					/*
					 * Loop and read whole thing
					 */
					cmd = in.readUTF();
				}
				catch(EOFException e){
					System.out.println("Exiting!");
					break;
				}
				
				System.out.println("Received: " + cmd);
				
				String res = c.dispatchCommand(cmd);
				if(cmd.equals("QUIT")){
					System.out.println("Exiting!");
					break;
				}
					
				DataOutputStream out = new DataOutputStream(usersock.getOutputStream());
				out.writeUTF(res);
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
				/*
				 * Waiting here, fix. Plus, handle EOF.
				 */
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