import java.util.logging.Logger;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.mongodb.*;
import java.net.UnknownHostException;
import java.text.ParseException;

public class CPU implements Runnable {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static CPU cpu = new CPU();
	private boolean isRunning;
	private Thread thread;
	private DB db;

	private Double LATITUDE_MAX = Constant.LATITUDE_MAX;
	private Double LATITUDE_MIN = Constant.LATITUDE_MIN;
	private Double LONGITUDE_MAX = Constant.LONGITUDE_MAX;
	private Double LONGITUDE_MIN = Constant.LONGITUDE_MIN;

	private CPU() {
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		thread = new Thread(this);
	}
	
	public static CPU getInstance(int cpu_id){
		return cpu;
	}

	public void start(){
		thread.start();
		isRunning = true;
	}
	
	@Override
	public void run(){
		DBCollection gpsData = db.getCollection("gps_data");
		while(isRunning){
			if(QueueTask.getInstance().isEmpty()){
				try{
					// logger.info("----QUEUE---"+QueueTask.getInstance().queueCount());
					Thread.sleep(1000);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}else{
				RawData raw_data = QueueTask.getInstance().popTask();
				int hours = parseDateTimeFromString(raw_data.getDateTime()).getHours();
				
				if(raw_data.getLatitude() < LATITUDE_MIN || raw_data.getLatitude() > LATITUDE_MAX) continue;
				if(raw_data.getLongitude() < LONGITUDE_MIN || raw_data.getLongitude() > LONGITUDE_MAX) continue;
				// if(((hours >= 21 && hours < 24 ) || (hours >= 0 && hours < 4)) && (raw_data.getType() == 1 || raw_data.getType() == 2)) continue;
				try{
					BasicDBObject doc = new BasicDBObject("device_id", raw_data.getDeviceId()).
																		append("latitude", raw_data.getLatitude()).
																		append("longitude", raw_data.getLongitude()).
																		append("speed", raw_data.getSpeed()).
																		append("reliability", raw_data.getReliability()).
																		append("satellite", raw_data.getSatellite()).
																		append("type", raw_data.getType()).
																		append("lock", raw_data.getLock()).
																		append("date_time", parseDateTimeFromString(raw_data.getDateTime())).
																		append("date_key", new BasicDBObject("date", parseDateFromString(raw_data.getDateTime())).append("frame", getFrame(raw_data.getDateTime()))).
																		append("option", raw_data.getOption());
			    gpsData.insert(doc);
		    }catch(Exception e){
					logger.info("Some thing went wrong with mongo!");
				}
			}
		}
	}

	public Date parseDateTimeFromString(String datetime){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = formatter.parse(datetime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public String parseDateFromString(String datetime){
		return datetime.substring(0, 10);
	}

	public int getFrame(String datetime){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			date = formatter.parse(datetime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int frame = (date.getHours())*4 + (int)((date.getMinutes())/15);
		return frame;
	}
}