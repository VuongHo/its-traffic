import com.mongodb.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
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
		QueueTask queue_task = new QueueTask();
		DBCollection gpsDataCo = db.getCollection("gps_data");
		DBCollection segmentSpeedCo = db.getCollection("segmentspeed");
		DBCollection segmentCellDetailsCo = db.getCollection("segment_cell_details");
		BasicDBObject query = new BasicDBObject("date_time", new BasicDBObject("$gte", lastMinutes(1)).append("$lt", timeNow())).
															append("lock", 1);
		DBCursor cursor = gpsDataCo.find(query);											
		try {
		  while(cursor.hasNext()) {
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	RawData raw_data 		= new RawData(raw_gps_data);
		  	queue_task.pushTask(raw_data);
		  }
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
			CPU cpu = new CPU(queue_task);
			cpu.start();
		  cursor.close();
		  logger.info("scheduler done..." + queue_task.queueCount());
		}
	}

	public String dateTimeCurrent(){
		SimpleDateFormat dateformat = new SimpleDateFormat ("yyyy-MM-dd");
		return dateformat.format(timeNow());
	}

	public int currentFrame(){
		return (timeNow().getHours())*4 + (int)((timeNow().getMinutes())/15);
	}

	public Date lastMinutes(int minutes){
		Calendar later = Calendar.getInstance();
   	later.add(Calendar.MINUTE, -minutes);
   	return later.getTime();
	}

	public Date lastSeconds(int seconds){
		Calendar later = Calendar.getInstance();
   	later.add(Calendar.SECOND, -seconds);
   	return later.getTime();
	}

	public Date timeNow(){
		return new Date();
	}
}
