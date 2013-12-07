import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CPU extends Thread {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private DB db;
	private DBCollection segmentspeed_co;
	private	DBCollection segment_cell_co;
	private Gson gson;
	private HashMap<String, SegmentSpeed> seg_speeds = new HashMap<>();

	public CPU(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		segmentspeed_co = db.getCollection("segmentspeed");
		segment_cell_co = db.getCollection("segment_cell_details");
	}

	@Override
	public void run(){
		logger.info("CPU starting...");
		int numOfgps = 0;
		int init_frame = currentFrame();
		DBCollection gpsDataCo = db.getCollection("gps_data");
		BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$gte", new ObjectId(lastMinutes(5)))).
																		 append("lock", 1);
		DBCursor cursor = gpsDataCo.find(query);										
		try {
		  while(cursor.hasNext()) {
		  	if(init_frame < currentFrame()) break;
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	processingRawData(new RawData(raw_gps_data), init_frame);

		  	numOfgps++;
		  }
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
			cursor.close();
			if(init_frame == currentFrame()) execSegmentSpeed();
			logger.info("CPU shutdown... " + numOfgps);
		}		
	}

	public void processingRawData(RawData raw_data, int current_frame) throws Exception {
		BasicDBObject query = findSegmentCellQuery(raw_data.getCellX(), raw_data.getCellY());
		String cell_key     = cellKey(raw_data.getCellX(), raw_data.getCellY());
		String segment_cell_cached =  (String) Memcache.getInstance().get(cell_key);
		
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
		Type type = new TypeToken<ArrayList<SegmentCell>>(){}.getType();

		if(segment_cell_cached == null){
			ArrayList<SegmentCell> seg_cacheds = new ArrayList<>();			
			DBCursor cursor = segment_cell_co.find(query);
			while(cursor.hasNext()) {
				SegmentCell segment = new SegmentCell((BasicDBObject) cursor.next());
				if(raw_data.nodeMatchSegment(segment)) execSegmentSpeedNoDB(segment, raw_data, current_frame);
				seg_cacheds.add(segment);
 			}
    	String json = gson.toJson(seg_cacheds, type);
    	Date expiring_time = Memcache.getInstance().expiringTime(15);
			Memcache.getInstance().add(cell_key, json, expiring_time);
		}else{
			ArrayList<SegmentCell> seg_cacheds = gson.fromJson(segment_cell_cached, type);
			for(SegmentCell segment : seg_cacheds){
				if(raw_data.nodeMatchSegment(segment)) execSegmentSpeedNoDB(segment, raw_data, current_frame);
			}
		}
	}

	public void execSegmentSpeedNoDB(SegmentCell segment, RawData raw_data, int current_frame){
		String seg_speed_key = raw_data.getDate() + Integer.toString(raw_data.getFrame()) + Integer.toString(segment.getSegmentId()) + Integer.toString(segment.getCellId());
		SegmentSpeed seg_speed = seg_speeds.get(seg_speed_key);
		if(seg_speed == null){
			seg_speed = new SegmentSpeed(segment.getSegmentId(),
																	 segment.getCellId(),
																	 raw_data.getCellX(),
																	 raw_data.getCellY(),
																	 segment.getStreetId(),
																	 raw_data.getSpeed(),
																	 raw_data.getDate(),
																	 current_frame);
			seg_speeds.put(seg_speed_key,seg_speed);
		}else{
			double speed = seg_speed.getSpeed();
			int sum = seg_speed.getSum() + 1;
			if (sum <= 30){
				seg_speed.setSpeed((speed+raw_data.getSpeed())/sum);
				seg_speed.setSum(sum);
				seg_speeds.remove(seg_speed_key);
				seg_speeds.put(seg_speed_key, seg_speed);
			}
		}
	}

	public void execSegmentSpeed(){
		for(String key : seg_speeds.keySet()){
			SegmentSpeed seg_speed = seg_speeds.get(key);
			BasicDBObject query = new BasicDBObject("date", seg_speed.getDate()).
																			 append("frame", seg_speed.getFrame()).
												    					 append("segment_id", seg_speed.getSegmentId()).
												    					 append("cell_id", seg_speed.getCellId());
			DBObject segment_speed = segmentspeed_co.findOne(query);
			if(segment_speed != null){
				int sum = (int)(segment_speed.get("sum")) + 1;
				segment_speed.put("speed", seg_speed.getSpeed());
				segment_speed.put("sum", sum);
				segmentspeed_co.save(segment_speed);

	  		// logger.info("----UPDATE--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId());
	  	}else{
	  		query = new BasicDBObject("segment_id", seg_speed.getSegmentId()).
								    			append("cell_id", seg_speed.getCellId()).
								    			append("cell_x", seg_speed.getCellX()).
								    			append("cell_y", seg_speed.getCellY()).
								    			append("street_id", seg_speed.getStreetId()).
								    			append("speed", seg_speed.getSpeed()).
								    			append("sum", 1).
								    			append("date", seg_speed.getDate()).
								    			append("frame", seg_speed.getFrame());
				segmentspeed_co.insert(query);
				
				// logger.info("----INSERT--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId());
			}
		}
	}

	public String cellKey(int cell_x, int cell_y){
		return Integer.toString(cell_x) + "minhvuong-cell" + Integer.toString(cell_y);
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
		ArrayList<BasicDBObject> cond = new ArrayList<BasicDBObject>();
		cond.add(new BasicDBObject("street.street_type", "motorway"));
		cond.add(new BasicDBObject("street.street_type", "motorway_link"));
		cond.add(new BasicDBObject("street.street_type", "trunk"));
		cond.add(new BasicDBObject("street.street_type", "trunk_link"));
		cond.add(new BasicDBObject("street.street_type", "primary"));
		cond.add(new BasicDBObject("street.street_type", "primary_link"));
		BasicDBObject query = new BasicDBObject("cell_x", cell_x).append("cell_y", cell_y).append("$or", cond);
		return query;
	}
	public int currentFrame(){
		return (timeNow().getHours())*4 + (int)((timeNow().getMinutes())/15);
	}

	public Date timeNow(){
		return new Date();
	}

	public String dateTimeCurrent(){
		SimpleDateFormat dateformat = new SimpleDateFormat ("yyyy-MM-dd");
		return dateformat.format(timeNow());
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
}