import com.mongodb.*;
import java.util.logging.Logger;

public class Scheduler {
	private static Logger logger = Logger.getLogger(Scheduler.class.getName());
	private static Scheduler scheduler = new Scheduler();
	private DB db;

	private Scheduler(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
	}
	
	public static Scheduler getInstance(){
		return scheduler;
	}
	
	public void schedule(){
		logger.info("schedule");
		CPU cpu = new CPU();
		cpu.start();
	}
}
