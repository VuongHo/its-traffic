
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.io.IOException;

public class ApplicationLog {
	private static ApplicationLog appLog = new ApplicationLog();
	private Logger logger = Logger.getLogger("ProcessingTrafficData");
	private FileHandler fh;

	public ApplicationLog(){
		try { 
      fh = new FileHandler("processing-traffic-data.log");
      logger.addHandler(fh);
      logger.setUseParentHandlers(false);
      SimpleFormatter formatter = new SimpleFormatter();  
      fh.setFormatter(formatter); 
      logger.severe("My first log");  
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
		logger.info(log);
	}
}