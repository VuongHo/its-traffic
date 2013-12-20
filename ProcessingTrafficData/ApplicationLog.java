
import java.io.*;

public class ApplicationLog {
	private static ApplicationLog appLog = new ApplicationLog();
	private File rawFile;

	public ApplicationLog(){
		try { 
      rawFile = new File("log/gps-no-matching.txt");
			if (!rawFile.getParentFile().exists()) rawFile.getParentFile().mkdirs();
			if(!rawFile.exists()) {
			  rawFile.createNewFile();
			}
    } catch (SecurityException e) {  
        e.printStackTrace();  
    } catch (IOException e) {  
        e.printStackTrace();  
    }  
	}

	public static ApplicationLog getInstance(){
		return appLog;
	}

	public void writeLog(String log){
		try { 
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rawFile, true)));
			out.println(log);
    	out.close();
    } catch (SecurityException e) {  
        e.printStackTrace();  
    } catch (IOException e) {  
        e.printStackTrace();  
    }
	}
}