import com.mongodb.*;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.ArrayList;

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
		BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$gte", new ObjectId(lastMinutes(15)))).
																		 append("lock", 1);
		DBCursor cursor = gpsDataCo.find(query);											
		try {
			ArrayList<RawData> data = new ArrayList<>();
		  while(cursor.hasNext()) {
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	data.add(new RawData(raw_gps_data));
		  }
		  if(data.size() > 0) QueueRawData.getInstance().pushTask(data);
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
		  logger.info("scheduler done...");
		}
	}

	public Date lastMinutes(int minutes){
		Calendar later = Calendar.getInstance();
   	later.add(Calendar.MINUTE, -minutes);
   	return later.getTime();
	}



	
}
