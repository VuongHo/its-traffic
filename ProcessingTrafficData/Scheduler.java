import com.mongodb.*;
import org.bson.types.ObjectId;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Iterator;

public class Scheduler {
	private static Logger logger = Logger.getLogger(Scheduler.class.getName());
	private static Scheduler scheduler = new Scheduler();
	private DB db;

	private boolean GENNERATE_VIRTUAL_DATA = Constant.GENNERATE_VIRTUAL_DATA;
	private boolean PRINT_LOG 						 = Constant.PRINT_LOG;
	private int LAST_MINUTE    						 = Constant.LAST_MINUTE;
	private int LIMIT_RECORDS 						 = Constant.LIMIT_RECORDS;

	private Scheduler(){
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
	}
	
	public static Scheduler getInstance(){
		return scheduler;
	}
	
	public void schedule(){
		Date last_minutes = lastMinutes(LAST_MINUTE);
		DBCollection gpsDataCo = db.getCollection("gps_data");
		BasicDBObject query = new BasicDBObject("_id", new BasicDBObject("$gte", new ObjectId(last_minutes))).
																		 append("lock", 1);
		DBCursor cursor = gpsDataCo.find(query).limit(LIMIT_RECORDS);
		try {
			ArrayList<RawData> data = new ArrayList<>();
			HashMap<String, ArrayList<RawData>> devices = new HashMap<>();;

		  while(cursor.hasNext()) {
		  	BasicDBObject raw_gps_data = (BasicDBObject) cursor.next();
		  	RawData raw_data = new RawData(raw_gps_data);
		  	if(raw_data.getDateTime() < last_minutes.getTime()) continue;
		  	if(raw_data.getSpeed().compareTo(0.0) > 0) data.add(raw_data);
		  	if(GENNERATE_VIRTUAL_DATA) devices = putRawDataToHash(devices, raw_data);
		  }
		  if(PRINT_LOG) System.out.println("TEST VIRTUAL DATA "+data.size());
		  for(String key : devices.keySet()){
				ArrayList<RawData> virtual_data = devices.get(key);
				if(virtual_data.size() < 2) continue;
				Collections.sort(virtual_data, new Comparator<RawData>() {
		        @Override
		        public int compare(RawData raw_data_1, RawData  raw_data_2)
		        {
		        		if(raw_data_1.getDateTime() > raw_data_2.getDateTime()) return 1;
		        		if(raw_data_1.getDateTime() < raw_data_2.getDateTime()) return -1;
		        		if(raw_data_1.getDateTime() == raw_data_2.getDateTime()) return 0;
		            return  0;
		        }
		    });
		    Iterator itr = virtual_data.iterator();
				while(itr.hasNext()){
					RawData vd1 = (RawData) itr.next();
					if(itr.hasNext()){
						RawData vd2 = (RawData) itr.next();
						int time = (int)((vd2.getDateTime() - vd1.getDateTime())/1000);
						Double distance = distanceBetweenTwoNode(vd1, vd2);
						Double vilocity = distance/time;
						// if(time >= 20 && time <= 40)logger.info("TEST distance:"+distanceBetweenTwoNode(vd1, vd2));
						if(time >= 25 && time <= 35 && distance.compareTo(30.0) > 0){

							double min_x = Math.abs(vd1.getLatitude() - vd2.getLatitude())/6;
							double min_y = Math.abs(vd1.getLongitude() - vd2.getLongitude())/6;
							for(int i = 1; i < 6; i++){
								RawData v_data = new RawData(vd1.getDeviceId(),
																						 vd1.getLatitude() + min_x*i,
																						 vd1.getLongitude() + min_y*i,
																						 vilocity*3.6,
																						 vd1.getReliability(),
																						 vd1.getSatellite(),
																						 vd1.getType(),
																						 vd1.getLock(),
																						 vd1.getDate(),
																						 vd1.getFrame(),
																						 new Date(vd1.getDateTime() + ((int)(time/6))*i*1000));
								data.add(v_data);
							}
						}
						if(vd2.getSpeed().compareTo(0.0) == 0){
							vd2.setSpeed(vilocity*3.6);
							data.add(vd2);
						}
					}
				}
			}
		  if(data.size() > 0) QueueRawData.getInstance().pushTask(data);
		  // System.out.println("TESt "+GENNERATE_VIRTUAL_DATA);
		  if(PRINT_LOG) System.out.println("TEST VIRTUAL DATA "+data.size());
		}catch(Exception e){
			logger.info("Some thing went wrong :" );
			e.printStackTrace();
		}finally{
		  logger.info("scheduler done...");
		}
	}

	public Double distanceBetweenTwoNode(RawData d1, RawData d2){
		return Math.sqrt(Math.pow(d1.getLatitude() - d2.getLatitude(), 2) + Math.pow(d1.getLongitude() - d2.getLongitude(), 2))*110000.00;
	}

	public HashMap<String, ArrayList<RawData>> putRawDataToHash(HashMap<String, ArrayList<RawData>> devices, RawData raw_data){
		ArrayList<RawData> list = devices.get(raw_data.getDeviceId());

		if(list == null){
			list = new ArrayList<>();
			list.add(raw_data);
		}else{
			list.add(raw_data);
		}
		devices.put(raw_data.getDeviceId(), list);
		return devices;
	}

	public Date lastMinutes(int minutes){
		Calendar later = Calendar.getInstance();
   	later.add(Calendar.MINUTE, -minutes);
   	return later.getTime();
	}

	public int currentFrame(){
		return (timeNow().getHours())*4 + (int)((timeNow().getMinutes())/15);
	}

	public Date timeNow(){
		return new Date();
	}
	
}
