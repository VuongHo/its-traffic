import com.mongodb.*;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CPU implements Runnable {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static CPU cpu1 = new CPU();
	private static CPU cpu2 = new CPU();
	private static CPU cpu3 = new CPU();
	private static CPU cpu4 = new CPU();
	private DB db;
	private boolean isRunning;
	private Thread thread;

	private CPU() {
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		thread = new Thread(this);
	}
	
	public static CPU getInstance(int cpu_id){
		logger.info("Start CPU"+cpu_id);
		switch(cpu_id){
			case 1:
				return cpu1;
			case 2:
				return cpu2;
			case 3:
				return cpu3;
			case 4:
			  return cpu4;
			default:
				return cpu1;
		}
	}

	public void start(){
		thread.start();
		isRunning = true;
	}
	
	@Override
	public void run(){
		while(isRunning){
			if(QueueTask.getInstance().isEmpty()){
				try{
					logger.info("----INSERT----"+QueueTask.getInstance().getIncrease2()+"---"+QueueTask.getInstance().getdate()+"---");
					logger.info("----UPDATE---"+QueueTask.getInstance().getIncrease());
					Thread.sleep(1000);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}else{
				Task gpsData = QueueTask.getInstance().popTask();
				if (gpsData.getFrame() < currentFrame()) continue;
				else{
					processingTask(gpsData);
				}
			}
		}
	}

	public void processingTask(Task gpsData){
		int cell_x  = gpsData.cellX();	
		int cell_y  = gpsData.cellY();
		String date = gpsData.getDate();
		int frame   = gpsData.getFrame();
		float speed = gpsData.getSpeed();
		DBCollection segmentspeed_co = db.getCollection("segmentspeed");
		DBCollection segment_cell_co = db.getCollection("segment_cell_details");
		DBCollection segment_co 		 = db.getCollection("segment");
		DBCollection street_co 			 = db.getCollection("street");
		BasicDBObject query 				 = findSegmentCellQuery(cell_x, cell_y);
		String cell_key 						 = Integer.toString(cell_x) + "v-d" + Integer.toString(cell_y);

		try{
			// Need cache here
			String segment_cell_cached = (String) Memcache.getInstance().get(cell_key);
			if (segment_cell_cached == null) {
				DBCursor cursor = segment_cell_co.find(query);

				if (cursor.length() == 0) return;

				String json = JSON.serialize(cursor);
				// Expire at next 1 minutes
				Date expire_at = Memcache.getInstance().expiringTime(15);
				Memcache.getInstance().add(cell_key, json, expire_at);
				segment_cell_cached = json;
			}
			
			JSONArray myjson = new JSONArray(segment_cell_cached);
			int json_count = myjson.length();
			for (int i= 0; i < json_count; i++){
        JSONObject segment_cell = myjson.getJSONObject(i);
		    if(gpsData.nodeBelongsToSegment(segment_cell)){
		    	String segmentspeed_key = date + "-" + Integer.toString(frame) + "-" + Integer.toString(segment_cell.getInt("segment_id")) + "-" + Integer.toString(segment_cell.getInt("cell_id"));
		    	String segmentspeed_cached = (String) Memcache.getInstance().get(segmentspeed_key);

		    	if (segmentspeed_cached == null) {
		    		// Caching & insert
		    		query = new BasicDBObject("segment_id", segment_cell.getInt("segment_id")).
							    										append("cell_id", segment_cell.getInt("cell_id")).
							    										append("cell_x", cell_x).
							    										append("cell_y", cell_y).
							    										append("street_id", (segment_cell.getJSONObject("street")).getInt("street_id")).
							    										append("speed", speed).
							    										append("sum", 1).
							    										append("date", date).
							    										append("frame", frame);
						segmentspeed_co.insert(query);
						QueueTask.getInstance().increase2();
						// logger.info("----INSERT--------"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id"));
						String ss_cached = JSON.serialize(query);
						// Expire at next 1 minutes
						Date expire_at = Memcache.getInstance().expiringTime(15);
						Memcache.getInstance().add(segmentspeed_key, ss_cached, expire_at);
		    	}else{
		    		// Updating
		    		JSONObject json_ss = new JSONObject(segmentspeed_cached);
		    		Double old_speed = json_ss.getDouble("speed");
		    		int sum = json_ss.getInt("sum") + 1;
		    		json_ss.put("speed", (speed+old_speed)/sum);
		    		json_ss.put("sum", sum);
		    		DBObject newSegmentSpeed = (DBObject) JSON.parse(json_ss.toString());
		    		segmentspeed_co.save(newSegmentSpeed);
		    		QueueTask.getInstance().increase();
		    		// logger.info("----UPDATE---"+QueueTask.getInstance().getIncrease()+"--"+QueueTask.getInstance().getdate()+"---"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id"));
		    		// Expire at next 1 minutes
						Date expire_at = Memcache.getInstance().expiringTime(1);
						Memcache.getInstance().replace(segmentspeed_key, segmentspeed_cached, expire_at);
		    	}
		    	
				}
			}
		}catch(Exception e){
			logger.info("Some thing went wrong with mongo!");
		}
	}

	public String MD5(String md5) {
   try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] array = md.digest(md5.getBytes());
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < array.length; ++i) {
        sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
      }
      return sb.toString();
    }catch (NoSuchAlgorithmException e) {
    }
    return null;
	}

	public BasicDBObject findSegmentCellQuery(int cell_x, int cell_y){
		BasicDBObject query = new BasicDBObject("cell_x", cell_x).append("cell_y", cell_y);
		return query;
	}
	
	public int currentFrame(){
		//Real Time
		Date today = new Date();
		int hour = today.getHours();
		int minute = today.getMinutes();
		int frame = hour*4 + (int)(minute/15);
		return frame;
	}
}