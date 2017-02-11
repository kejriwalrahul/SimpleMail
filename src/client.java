import java.util.*;
import java.io.*;
import java.net.*;

/*
 *	Main class
 *		Implements client side 
 */
public class client{
	static Scanner stdin = new Scanner(System.in);
	
	// Flag to check if in user subprompt.
	static boolean subprompt = false;
	// Current user
	static String userid = "";
	
	/*
	 *  Function to print error msg and exit on error with non-zero error code.
	 *  Used as last resorts.
	 *  Prefer showing error msgs.
	 */
	static void errorExit(String s){
		System.err.println("Error: " + s);
		System.exit(1);
	}
	
	/*
	 *  Implements client side interface (Command Parser)
	 *  Returns command to be sent to the server side.
	 */
	static String userInterface(){
		String str = "";
		
		/*
			Main Interface
		*/
		if(!subprompt){
			System.out.print("Main-Prompt> ");
			str = stdin.nextLine();

			String[] toks = str.split(" ");
			String cmd = toks[0];
			String arg = "";
			if(toks.length > 1)
				arg = toks[1];
			
			if(cmd.equals("Listusers"))
				str = "LSTU";
			else if(cmd.equals("Adduser"))
				str = "ADDU " + arg;
			else if(cmd.equals("SetUser")){
				str = "USER " + arg;
				subprompt = true;
				userid = arg;
			}
			else if(cmd.equals("Quit"))
				str = "QUIT";
			// Throw err msg, for unrecognized commands
			else
				return "Unknown Command!";
		}
		/*
			Sub-prompt interface
		*/
		else{
			System.out.print("Sub-Prompt-"+userid+"> ");
			str = stdin.nextLine();

			String[] toks = str.split(" ");
			String cmd = toks[0];
			String arg = "";
			if(toks.length > 1)
				arg = toks[1];				
			
			if(cmd.equals("Read"))
				str = "READM";
			else if(cmd.equals("Delete"))
				str = "DELM";
			else if(cmd.equals("Send")){
				String subject, msg;
				
				// Get subject
				System.out.print("\tType Subject: ");
				subject = stdin.nextLine();
				
				// Get Msg, Loop till we receive ### delimiter
				System.out.print("\tType Message: ");
				msg = stdin.nextLine();
				while(!msg.endsWith("###")){
					System.out.print("\t         ...: ");
					msg += stdin.nextLine();
					msg = msg.trim();
				}
				
				str = "SEND " + arg + " " + subject + " ### " + msg;
			}
			else if(cmd.equals("Done")){
				str = "DONEU";
				subprompt = false;
				userid = "";
			}
			// Throw err msg, for unrecognized commands
			else
				return "Unknown Command!";
		}
		
		// Returns command to be xmitted to server
		return str;
	}
	
	/*
	 *  Network Interface
	 *  Establishes connections and xmits and receives msgs from server.
	 */
	public static void main(String[] args){
		String  server 	= args[0];
		int 	port	= Integer.parseInt(args[1]);
		
		String sendstr = "";
		boolean quit = false;
		
		try{
			System.out.println("About to open " + server + " at " + Integer.toString(port));
			
			// Establish sockets for communication
			Socket s = new Socket(server, port);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream  in  = new DataInputStream(s.getInputStream());
			
			// Communicate on established connection, as long as reqd 
			while(true){
				// Get cmd to be xmitted
				sendstr = userInterface();
				if(sendstr.equals("Unknown Command!")){
					System.out.println("Unknown Command!");
					continue;
				}
				
				/*
					Each cmd is given a '#### ###' trailer to recognize msg end.
				*/
				sendstr = sendstr + " #### ###";
				if(sendstr.equals("QUIT #### ###"))
					quit = true;
				
				/*
					Max len msg = 65536 since UTF uses only 2 bytes for length.
					If msg.len > MAX_LEN, split and send in mulitple rounds.
				*/
				while(!sendstr.equals("")){
					if(sendstr.length() > 65530){
						out.writeUTF(sendstr.substring(0, 65530));
						sendstr = sendstr.substring(65530);
					}
					else{
						out.writeUTF(sendstr);
						sendstr = "";
					}							
				}
				
				/*
					Exit on quit cmd.
				*/
				if(quit)
					break;
								
				// Initialize empty buffer
				String res = "";
				
				// Receive pkts till msg terminator
				while(!(res = in.readUTF()).contains("#### ###"));
				
				// Remove terminator
				res = res.substring(0, res.indexOf("#### ###"));
				
				// If cmd fails and user is set, exit subprompt. 
				if(res.contains("User does not exist!") || res.contains("Syntax Error"))
					subprompt = false;
				
				// Display response
				System.out.println("Response: " + res);
			}
			// out.writeUTF("QUIT");
			
			// Close connection
			s.close();
		}
		catch(EOFException e){
			// System.out.println("Sent: " + sendstr);
			// e.printStackTrace();
			
			// If server has crashed, EOF appears in socket streams.
			System.out.println("Possible Server Crash!");
		}
		catch(IOException e){
			System.out.println("\nError received. Please find details:");
			e.printStackTrace();
		}
	}
}