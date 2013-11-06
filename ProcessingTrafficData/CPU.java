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
import com.google.gson.GSON;

public class CPU extends Thread {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private DB db;
	private DBCollection segmentspeed_co;
	private	DBCollection segment_cell_co;
	private Gson gson;

	public CPU(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		segmentspeed_co = db.getCollection("segmentspeed");
		segment_cell_co = db.getCollection("segment_cell_details");
		gson = new Gson();
	}

	@Override
	public void run(){
		logger.info("CPU starting...");
		while(!QueueTask.getInstance().isEmpty()){
			try{
				RawData raw_data = QueueTask.getInstance().popTask();
				if (raw_data.getFrame() < currentFrame()) continue;
				else{
					processingRawData(raw_data);
				}
			}catch(Exception e){
				logger.info("Some thing went wrong :" );
				e.printStackTrace();
			}
		}
		logger.info("CPU shutdown...");
	}

	public void processingRawData(RawData raw_data) throws Exception {
		BasicDBObject query = findSegmentCellQuery(raw_data.getCellX(), raw_data.getCellY());
		String cell_key     = cellKey(raw_data.getCellX(), raw_data.getCellY());
		String segment_cell_cached =  (String) Memcache.getInstance().get(cell_key);
		// Caching cell
		if (segment_cell_cached == null) {
			DBCursor cursor = segment_cell_co.find(query);
			String json_sc     = JSON.serialize(cursor);
			Date expiring_time = Memcache.getInstance().expiringTime(15);
			Memcache.getInstance().add(cell_key, json_sc, expiring_time);
			segment_cell_cached = json_sc;
		}else{

		}

		execWithSegmentCellCached(segment_cell_cached, raw_data);
	}

	public void execWithSegmentCellCached(String segment_cell_cached, RawData raw_data) throws Exception{
		JSONArray json_arr_sc = new JSONArray(segment_cell_cached);
		int count_sc     			= json_arr_sc.length(); 
		for(int i = 0; i < count_sc; i++){
			JSONObject segment_cell = json_arr_sc.getJSONObject(i);
			if(raw_data.nodeMatchSegment(segment_cell)){
				// Caching segment
				String segment_key = cell_key + "-segment_key";
				execSegmentSpeed(segment_cell, raw_data);	
			}
		}
	}

	public void execSegmentSpeed(JSONObject segment_cell, RawData raw_data) throws Exception{
		BasicDBObject query = findSegmentSpeedQuery(raw_data, segment_cell);
  	DBObject segment_speed = segmentspeed_co.findOne(query);

  	if(segment_speed != null){
  		updateSegmentSpeed(segment_speed, raw_data);
  		String log = "----UPDATE--------"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id");
  		ApplicationLog.getInstance().writeLog(log);
  	}else{
  		query = insertSegmentSpeedQuery(raw_data, segment_cell);
			insertSegmentSpeed(query);
			String log = "----INSERT--------"+ segment_cell.getInt("segment_id") + " " + segment_cell.getInt("cell_id");
			ApplicationLog.getInstance().writeLog(log);
		}
	}

	public BasicDBObject insertSegmentSpeedQuery(RawData raw_data, JSONObject segment_cell) throws Exception{
		return new BasicDBObject("segment_id", segment_cell.getInt("segment_id")).
						    			append("cell_id", segment_cell.getInt("cell_id")).
						    			append("cell_x", raw_data.getCellX()).
						    			append("cell_y", raw_data.getCellY()).
						    			append("street_id", (segment_cell.getJSONObject("street")).getInt("street_id")).
						    			append("speed", raw_data.getSpeed()).
						    			append("sum", 1).
						    			append("date", raw_data.getDate()).
						    			append("frame", raw_data.getFrame());
	}

	public BasicDBObject findSegmentSpeedQuery(RawData raw_data, JSONObject segment_cell) throws Exception{
		return new BasicDBObject("date", raw_data.getDate()).
											append("frame", raw_data.getFrame()).
				    					append("segment_id", segment_cell.getInt("segment_id")).
				    					append("cell_id", segment_cell.getInt("cell_id"));
	}

	public void insertSegmentSpeed(BasicDBObject data){
		segmentspeed_co.insert(data);
	}

	public void updateSegmentSpeed(DBObject segment_speed, RawData raw_data){
		double old_speed = (double)(segment_speed.get("speed"));
		int sum = (int)(segment_speed.get("sum")) + 1;
		segment_speed.put("speed", (float)((raw_data.getFrame()+old_speed)/sum));
		segment_speed.put("sum", sum);
		segmentspeed_co.save(segment_speed);
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
		BasicDBObject query = new BasicDBObject("cell_x", cell_x).append("cell_y", cell_y);
		return query;
	}
	public int currentFrame(){
		return (timeNow().getHours())*4 + (int)((timeNow().getMinutes())/15);
	}

	public Date timeNow(){
		return new Date();
	}
}