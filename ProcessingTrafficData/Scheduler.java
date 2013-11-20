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
		DBCollection gpsDataCo = db.getCollection("gps_data");
		DBCollection segmentSpeedCo = db.getCollection("segmentspeed");
		DBCollection segmentCellDetailsCo = db.getCollection("segment_cell_details");
		BasicDBObject query = new BasicDBObject("date_time", new BasicDBObject("date", dateTimeCurrent()).append("frame", currentFrame())).
															append("status", false);
		DBCursor cursor = gpsDataCo.find(query);											
		try {
		  while(cursor.hasNext()) {
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	RawData raw_data 		= new RawData(raw_gps_data);
		  	QueueTask.getInstance().pushTask(raw_data);
		  	raw_gps_data.put("status", true);
				gpsDataCo.save(raw_gps_data);
		  }
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
		  cursor.close();
		  logger.info("has scheduler...");
		  String log = "The number of raw data\'s data: " + QueueTask.getInstance().queueCount() + " :)";
			ApplicationLog.getInstance().writeLog(log);
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
