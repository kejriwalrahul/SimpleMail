import java.util.*;
import java.io.*;
import java.net.*;

public class client{
	static Scanner stdin = new Scanner(System.in);
	static boolean subprompt = false;
	static String userid = "";
	
	static void errorExit(String s){
		System.err.println("Error: " + s);
		System.exit(1);
	}
	
	static String userInterface(){
		String str = "";
		
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
			else
				return "Unknown Command!";
		}
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
				
				System.out.print("\tType Subject: ");
				subject = stdin.nextLine();
				
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
			else
				return "Unknown Command!";
		}
		
		return str;
	}
	
	public static void main(String[] args){
		String  server 	= args[0];
		int 	port	= Integer.parseInt(args[1]);
	
		try{
			System.out.println("About to open " + server + " at " + Integer.toString(port));
			
			Socket s = new Socket(server, port);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream  in  = new DataInputStream(s.getInputStream());
			
			while(true){
				String sendstr = userInterface();
				if(sendstr.equals("Unknown Command!")){
					System.out.println("Unknown Command!");
					continue;
				}
				
				out.writeUTF(sendstr);				
				if(sendstr.equals("QUIT"))
					break;
				
				String res = in.readUTF();
				if(res.contains("User does not exist!") || res.contains("Syntax Error"))
					subprompt = false;
				
				System.out.println("Response: " + res);
			}
			out.writeUTF("QUIT");
			
			s.close();
		}
		catch(EOFException e){
			System.out.println("Possible Server Crash!");
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}