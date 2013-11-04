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

public class CPU extends Thread {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private DB db;

	public CPU(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
	}

	@Override
	public void run(){
		logger.info("CPU starting...");
		DBCollection gpsDataCo = db.getCollection("gps_data");
		DBCollection segmentSpeedCo = db.getCollection("segmentspeed");
		DBCollection segmentCellDetailsCo = db.getCollection("segment_cell_details");
		BasicDBObject query = new BasicDBObject("trktime", new BasicDBObject("$gte", lastMinutes(1)).append("$lt", timeNow())).
															append("date_time", new BasicDBObject("date", dateTimeCurrent()).append("frame", currentFrame()));
		DBCursor cursor = gpsDataCo.find(query);												
		try {
		  while(cursor.hasNext()) {
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	RawData raw_data 		= new RawData(raw_gps_data);
		  	
		  	processingRawData(raw_data);
		  }
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
		   cursor.close();
		   logger.info("CPU shutdown...");
		}
	}

	public void processingRawData(RawData raw_data) throws Exception {
		DBCollection segmentspeed_co = db.getCollection("segmentspeed");
		DBCollection segment_cell_co = db.getCollection("segment_cell_details");
		BasicDBObject query = findSegmentCellQuery(raw_data.getCellX(), raw_data.getCellY());
		String cell_key     = cellKey(raw_data.getCellX(), raw_data.getCellY());
		String segment_cell_cached =  (String) Memcache.getInstance().get(cell_key);

		if (segment_cell_cached == null) {
			DBCursor cursor = segment_cell_co.find(query);
			if ( cursor.length() == 0) return;
			String json_sc     = JSON.serialize(cursor);
			Date expiring_time = Memcache.getInstance().expiringTime(15);
			Memcache.getInstance().add(cell_key, json_sc, expiring_time);
			segment_cell_cached = json_sc;
		}

		JSONArray json_arr_sc = new JSONArray(segment_cell_cached);
		int count_sc     			= json_arr_sc.length(); 
		for(int i = 0; i < count_sc; i++){
			JSONObject segment_cell = json_arr_sc.getJSONObject(i);
			if(raw_data.nodeMatchSegment(segment_cell)){
				query = new BasicDBObject("date", raw_data.getDate()).append("frame", raw_data.getFrame()).
				    																		append("segment_id", segment_cell.getInt("segment_id")).
				    																		append("cell_id", segment_cell.getInt("cell_id"));
	    	DBObject segment_speed = segmentspeed_co.findOne(query);

	    	if(segment_speed != null){
	    		double old_speed = (double)(segment_speed.get("speed"));
	    		int sum = (int)(segment_speed.get("sum")) + 1;
	    		segment_speed.put("speed", (float)((raw_data.getFrame()+old_speed)/sum));
	    		segment_speed.put("sum", sum);
	    		segmentspeed_co.save(segment_speed);
	    		logger.info("----UPDATE--------"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id"));
	    	}else{
	    		query = new BasicDBObject("segment_id", segment_cell.getInt("segment_id")).
						    										append("cell_id", segment_cell.getInt("cell_id")).
						    										append("cell_x", raw_data.getCellX()).
						    										append("cell_y", raw_data.getCellY()).
						    										append("street_id", (segment_cell.getJSONObject("street")).getInt("street_id")).
						    										append("speed", raw_data.getSpeed()).
						    										append("sum", 1).
						    										append("date", raw_data.getDate()).
						    										append("frame", raw_data.getFrame());
					segmentspeed_co.insert(query);
					logger.info("----INSERT--------"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id"));
				}		
			}
		}
	}

	public String cellKey(int cell_x, int cell_y){
		return Integer.toString(cell_x) + "v-d" + Integer.toString(cell_y);
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

	public Date timeNow(){
		return new Date();
	}
}