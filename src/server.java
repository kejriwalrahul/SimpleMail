import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

/*
 * To do:
 * 		1. [DONE] Loop while receiving packets
 * 		2. [DONE] Send mail to urself: works apparently
 */

/*
 *  Class to parse commands at server side and
 *  	execute corresponding actions
 */
class CommandParser{
	
	// Current userid
	String userid = "";

	//	Current User File
	RandomAccessFile fil;
	File fileObj;
	
	/*
	 * 	Function to print error msg and exit with non-zero error code.
	 *  To be used as last resort
	 *  Prefer to send error msgs to clients
	 */
	void errorExit(String s){
		System.err.println("Error: " + s);
		System.exit(1);
	}
	
	// Function to list users known to the system
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
	
	// Function to add user to system, if not already existing
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
	
	// Function to count no. of msgs in current spool file. Seeks back to file beginning post counting.
	int countMsgs(){
		String line;
		int count = 0;
		
		try{
			// Counts # of msg delimiters
			while((line = fil.readUTF()) != null){
				if(line.trim().contains("###"))
					count++;
			}
		}
		catch(EOFException e){
			try{
				// Goto file start on reaching end
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
		
		// Server monitoring msg
		System.out.println("No of msgs: " + Integer.toString(count) + "\n");
		
		// Returns # of msgs in user file
		return count;
	}
	
	// Sets current user, if exists.
	String setCurrUser(String u){
		String res = "";
		
		// If any user is active, deactivate him/her
		if(userid != "")
			closeUser();
		
		// Check if user exists
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
			
			// Open user file
			fil = new RandomAccessFile(f, "rw");
			fileObj = f;
		}
		catch(IOException e){
			e.printStackTrace();
			errorExit("File Open Error!");
		}
		
		// Return ack msg with # of user msgs
		return "User " + u + " exists and has " + Integer.toString(countMsgs()) + " number of messages in his/her spool file";
	}
	
	// Reads current msg
	String readMsg(){
		String res = "";
		
		String s = "\n";
		int idx;
		try{
			// Reads off msg
			while((s = fil.readUTF()) != null){
				if(s.trim().contains("###")){
					res += s;
					break;
				}
				res += s;
			}
		}
		catch(EOFException e){
			// On reaching end of file, do nothing, send back read string
			// Failsafe, should reach here only when no msg is left in spool file.
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		// If string read is not a msg
		if(!res.contains("###"))
			res = "No More Mail";
		
		return res;
	}
	
	// Deletes current msg
	String delMsg(){
		RandomAccessFile tempfil;
		boolean delflag = false;
		long offset = 0;
		
		// Create temp file for copying remaining mails
		File temp = new File("./temp");
		
		try{
			// Create and open temp file
			temp.createNewFile();
			tempfil = new RandomAccessFile(temp, "rw");
			
			// Get position of msg to be deleted
			offset = fil.getFilePointer();
			long current_offset = 0;
			// Goto file start
			fil.seek(0);
			
			// Copy mails before one to be deleted
			while(current_offset != offset){
				tempfil.writeUTF(fil.readUTF());
				current_offset = fil.getFilePointer();
			}
			
			// Skip Msg to be deleted
			while(!fil.readUTF().contains("###"));
			delflag = true;
			
			// Copy mail after msg to be deleted
			String line = "";
			while(!(line = fil.readUTF()).isEmpty()){
				tempfil.writeUTF(line);
			}
			
			// Close temp file
			tempfil.close();
		}
		catch(EOFException e){
			// If no file to be deleted 
			if(!delflag)
				return "No More Mail";
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		try{
			// Close user file
			fil.close();
			// Delete user file
			fileObj.delete();
			// Create new user file object
			fileObj = new File("./" + userid + ".dat");
			// Rename temp file to newly created user file object 
			temp.renameTo(fileObj);
			// Reopen (new) user file
			fil = new RandomAccessFile(fileObj, "rw");
			// Set file pointer back to original position
			fil.seek(offset);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return "Message Deleted";
	}
	
	/*
	 * Function to send msg, if recipient exists.
	 * Issue:
	 * 		[FIXED] What if sending urself?
	 * 
	*/
	String sendMsg(String cmd){
		String[] parts = cmd.split("###");
		String[] toks  = parts[0].split(" ");
		
		// Parse the receiver of mail.
		String recvr = toks[1];

		// Parse the subject portion of command.
		String subj = "";
		for(int i=2;i<toks.length;i++)
			subj += toks[i] + " ";
		subj = subj.trim();
		
		// Parse the msg body portion of command.
		String msg  = parts[1] + "\n###";
		
		// Open receiver spool file, if exists.
		File recvf = new File("./" + recvr + ".dat");
		if(!recvf.exists())
			return "Reciever does not exist!";
		
		// Try writing mail.
		try{
			RandomAccessFile recvfil = new RandomAccessFile(recvf, "rw");
			
			// Goto end of file
			recvfil.seek(recvfil.length());
			
			// Write headers
			recvfil.writeUTF("\nFrom: " + userid.trim() + "\n");
			recvfil.writeUTF("To: " + recvr.trim() + "\n");
			recvfil.writeUTF("Date: " + (new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").format(new Date()))+"\n");
			recvfil.writeUTF("Subject: " + subj.trim() + "\n");
			
			// Remove trailing spaces in msg
			msg = msg.trim();

			// Write msg to file 
			while(!msg.equals("")){
				if(msg.length() > 65530){
					recvfil.writeUTF(msg.substring(0, 65530));
					msg = msg.substring(65530);
				}
				else{
					recvfil.writeUTF(msg);
					msg = "";
				}
			}
			recvfil.writeUTF("\n");
			
			// Close file
			recvfil.close();
			
			return "Message Sent";
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return "Error";
	}
	
	// Closes down the current user.
	String closeUser(){
		String res = "Close Successful";
		
		// Set user to empty
		userid =  "";
		try{
			// Close user file
			fil.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return res;
	}
	
	// Dispatches command to relevant action. Raises error msg on inappropriate usage.
	String dispatchCommand(String cmd){
		String[] tokens = cmd.split(" ");
		
		// For commands sent with arguments
		if(tokens.length >= 2){
			if(tokens[0].equals("ADDU"))			return addUser(tokens[1]);
			else if(tokens[0].equals("USER"))		return setCurrUser(tokens[1]);
			else if(tokens[0].equals("SEND"))		return sendMsg(cmd);
			// For unrecognized commands, send err msg
			else 									return "Syntax Error!";
		}
		// For commands sent without arguments
		else{
			if(tokens[0].equals("LSTU"))			return listUsers();
			else if(tokens[0].equals("READM"))		return readMsg();
			else if(tokens[0].equals("DELM"))		return delMsg();
			else if(tokens[0].equals("DONEU"))		return closeUser();
			else if(tokens[0].equals("QUIT"))		return "";
			// For unrecognized commands, send err msg
			else 									return "Syntax Error";
		}
	}
}

/*
 * 	Server class to handle each incoming connection.
 *  Allows multiple clients to run concurrently each having their own active users.
 */
class Server extends Thread{
	public ServerSocket sock;
	public Socket usersock;
	
	/*
	 *  Creates a command parser for current incoming client.
	 */
	public void run(){
		try{	
			CommandParser c = new CommandParser();
			while(true){
				DataInputStream in = new DataInputStream(usersock.getInputStream());
				String cmd;
				
				try{
					/*
					 * Loop and read whole msg sent by the client
					 */
					
					// Initialize empty buffer
					cmd = "";
					
					// Receive pkts till msg terminator is read
					while(!(cmd += in.readUTF()).contains("#### ###"));
					
					// Remove terminator
					cmd = cmd.substring(0, cmd.indexOf("#### ###"));
					cmd = cmd.trim();
				}
				catch(EOFException e){
					// If user closed connection
					System.out.println("Exiting!");
					break;
				}
				
				// Server monitoring msg
				System.out.println("Received: " + cmd);
				
				// Execute received command
				String res = c.dispatchCommand(cmd);
				// Close connection on QUIT
				if(cmd.equals("QUIT")){
					System.out.println("Exiting!");
					break;
				}
					
				// Get stram to reply to user
				DataOutputStream out = new DataOutputStream(usersock.getOutputStream());
				// Append delimiter to msg
				res = res + "#### ###";
				
				// Split msg and send till nothing is left to send
				while(!res.equals("")){
					if(res.length() > 65530){
						out.writeUTF(res.substring(0, 65530));
						res = res.substring(65530);
						
					}
					else{
						out.writeUTF(res);
						res = "";
					}							
				}
			}
			// Close user socket
			usersock.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}	
}

/*
 * 	Main class
 * 		Accepts connections and creates a Server object in its own thread for each incoming connection.
 * 
 */
public class server{
	public static void main(String[] args) throws IOException{
		// Get port
		int port = Integer.parseInt(args[0]);
		
		try{			
			// Open master socket for server 
			ServerSocket sock = new ServerSocket(port, 5);
			// Client(slave) socket
			Socket usersock;
			Server s;
			
			while(true){
				try{
					// Accept TCP connection
					usersock = sock.accept();
					// Create child to serve received user
					s = new Server();
					// Set child's sockets fields
					s.sock = sock;
					s.usersock = usersock;
					// Start the child thread
					s.start();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}