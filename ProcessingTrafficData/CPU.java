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
	private QueueTask queue_task;
	private HashMap<String, SegmentSpeed> seg_speeds = new HashMap<>();

	public CPU(QueueTask queue_task){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		segmentspeed_co = db.getCollection("segmentspeed");
		segment_cell_co = db.getCollection("segment_cell_details");
		this.queue_task = queue_task;
	}

	@Override
	public void run(){
		logger.info("CPU starting...");
		while(!queue_task.isEmpty()){
			try{
				RawData raw_data = queue_task.popTask();
				processingRawData(raw_data);
			}catch(Exception e){
				logger.info("Some thing went wrong :" );
				e.printStackTrace();
			}
		}
		execSegmentSpeed();
		logger.info("CPU shutdown...");
	}

	public void processingRawData(RawData raw_data) throws Exception {
		BasicDBObject query = findSegmentCellQuery(raw_data.getCellX(), raw_data.getCellY());
		String cell_key     = cellKey(raw_data.getCellX(), raw_data.getCellY());
		String segment_cell_cached =  (String) Memcache.getInstance().get(cell_key);
		Double cell0_lat = 10.609309 + raw_data.getCellX()*0.01;
		Double cell0_lon = 106.493811 + raw_data.getCellY()*0.01;
		HashMap<String, List<SegmentCell>> seg_cached = new HashMap<>();
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.enableComplexMapKeySerialization().setPrettyPrinting().create();
		Type type = new TypeToken<HashMap<String,List<SegmentCell>>>(){}.getType();

		if(segment_cell_cached == null){			
			DBCursor cursor = segment_cell_co.find(query);
			SegmentCell segment;
			while(cursor.hasNext()) {
				segment = new SegmentCell((BasicDBObject) cursor.next());
				if(raw_data.nodeMatchSegment(segment)) execSegmentSpeedNoDB(segment, raw_data);

				// TODO
				int seg_cx = (int) ((segment.getSNodeLat() - cell0_lat)/0.001);
				int seg_cy = (int) ((segment.getSNodeLon() - cell0_lon)/0.001);
				if (seg_cx < 0 || seg_cy < 0 ){
					seg_cx = (int) ((segment.getENodeLat() - cell0_lat)/0.001);
					seg_cy = (int) ((segment.getENodeLon() - cell0_lon)/0.001);
				}

				if(seg_cx >= 0 && seg_cy >= 0 ){
					String seg_key = seg_cx + "minhvuong-seg" + seg_cy;
					List<SegmentCell> segs = seg_cached.get(seg_key);
					if (segs == null){
						ArrayList<SegmentCell> new_segs = new ArrayList<>();
						new_segs.add(segment);
						seg_cached.put(seg_key,new_segs);
					}else{
						segs.add(segment);
						seg_cached.remove(seg_key);
						seg_cached.put(seg_key, segs);
					}

				}
 			}
    	String json = gson.toJson(seg_cached, type);
    	Date expiring_time = Memcache.getInstance().expiringTime(15);
			Memcache.getInstance().add(cell_key, json, expiring_time);
		}else{
			seg_cached = gson.fromJson(segment_cell_cached, type);
			// TODO
			int seg_cx = (int) ((raw_data.getLatitude() - cell0_lat)/0.001);
			int seg_cy = (int) ((raw_data.getLongitude() - cell0_lon)/0.001);
			if (seg_cx >= 0 && seg_cy >= 0 ){
				String seg_key = seg_cx + "minhvuong-seg" + seg_cy;
				List<SegmentCell> segs = seg_cached.get(seg_key);
				if (segs != null){
					for(SegmentCell segment : segs){
						if(raw_data.nodeMatchSegment(segment)) execSegmentSpeedNoDB(segment, raw_data);
					}
				}
			}
		}
	}

	public void execSegmentSpeedNoDB(SegmentCell segment, RawData raw_data){
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
																	 raw_data.getFrame());
			seg_speeds.put(seg_speed_key,seg_speed);
		}else{
			double speed = seg_speed.getSpeed();
			int sum = seg_speed.getSum() + 1;
			seg_speed.setSpeed((speed+raw_data.getSpeed())/sum);
			seg_speed.setSum(sum);
			seg_speeds.remove(seg_speed_key);
			seg_speeds.put(seg_speed_key, seg_speed);
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

	  		String log = "----UPDATE--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId();
	  		ApplicationLog.getInstance().writeLog(log);
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
				
				String log = "----INSERT--------"+ seg_speed.getSegmentId() + " " + seg_speed.getCellId();
				ApplicationLog.getInstance().writeLog(log);
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