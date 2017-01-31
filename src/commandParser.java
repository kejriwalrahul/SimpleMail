import java.util.*;
import java.io.*;

public class commandParser{
	String currUser;
	
	String listUsers(){
		String res = "";
		File dir = new File(".");
		File[] dirFiles = dir.listFiles();
		
		for(File fil: dirFiles){
			if(fil.isFile() && fil.getName().contains(".dat"))
				res += fil.getName().substring(0, fil.getName().indexOf(".dat"));
		}
		
		return res;
	}
}