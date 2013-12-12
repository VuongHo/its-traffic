import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class CpuRealTime implements Runnable {
	private static Logger logger = Logger.getLogger(CpuRealTime.class.getName());
	private static CpuRealTime cpu = new CpuRealTime();
	private DBCollection segmentspeed_co;
	private Thread thread;
	private boolean isRunning;
	private DB db;

	public CpuRealTime(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		thread = new Thread(this);
		segmentspeed_co = db.getCollection("segmentspeed");
	}

	public static CpuRealTime getInstance(){
		return cpu;
	}

	public void start(){
		thread.start();
		isRunning = true;
	}

	@Override
	public void run(){
		logger.info("CpuRealTime starting...");
		while(isRunning){
			try{
				if(QueueTask.getInstance().isEmpty()){
					try{
						// logger.info("die--");
						Thread.sleep(1000);
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}else{
					HashMap<String, SegmentSpeed> seg_speeds = QueueTask.getInstance().popTask();
					execSegmentSpeed(seg_speeds);
				}
			}catch(Exception e){
				logger.info("Some thing went wrong :" );
				e.printStackTrace();
			}
		}
		logger.info("CpuRealTime shutdown...");
	}

	public void execSegmentSpeed(HashMap<String, SegmentSpeed> seg_speeds){
		for(String key : seg_speeds.keySet()){
			SegmentSpeed seg_speed = seg_speeds.get(key);
			// if(Double.compare(seg_speed.getSpeed(),0.00) == 0) continue;
			BasicDBObject query = new BasicDBObject("date", seg_speed.getDate()).
																			 append("frame", seg_speed.getFrame()).
												    					 append("segment_id", seg_speed.getSegmentId()).
												    					 append("cell_id", seg_speed.getCellId());
			DBObject segment_speed = segmentspeed_co.findOne(query);
			if(segment_speed != null){
				segment_speed.put("speed", seg_speed.getSpeed());
				segment_speed.put("sum", seg_speed.getSum());
				segmentspeed_co.save(segment_speed);

	  		// logger.info("----UPDATE--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId());
	  	}else{
	  		query = new BasicDBObject("segment_id", seg_speed.getSegmentId()).
								    			append("cell_id", seg_speed.getCellId()).
								    			append("cell_x", seg_speed.getCellX()).
								    			append("cell_y", seg_speed.getCellY()).
								    			append("street_id", seg_speed.getStreetId()).
								    			append("speed", seg_speed.getSpeed()).
								    			append("sum", seg_speed.getSum()).
								    			append("date", seg_speed.getDate()).
								    			append("frame", seg_speed.getFrame());
				segmentspeed_co.insert(query);
				
				// logger.info("----INSERT--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId());
			}
		}
	}

	public int currentFrame(){
		return (timeNow().getHours())*4 + (int)((timeNow().getMinutes())/15);
	}

	public Date timeNow(){
		return new Date();
	}
}