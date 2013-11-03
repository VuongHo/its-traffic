import java.util.logging.Logger;
import java.util.Date;
import com.mongodb.*;
import java.net.UnknownHostException;

public class CPU implements Runnable {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static CPU cpu = new CPU();
	private boolean isRunning;
	private Thread thread;
	private DB db;

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
					logger.info("idle");
					Thread.sleep(1000);
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}else{
				RawData raw_data = QueueTask.getInstance().popTask();
				if (raw_data.getFrame() < currentFrame()) continue;
				else{
					try{
						BasicDBObject doc = new BasicDBObject("device_id", raw_data.getDeviceId()).
																			append("latitude", raw_data.getLatitude()).
																			append("longitude", raw_data.getLongitude()).
																			append("speed", raw_data.getSpeed()).
																			append("satellite", raw_data.getSatellite()).
																			append("lock", raw_data.getLock()).
																			append("trktime", raw_data.getTrktime()).
																			append("date_time", new BasicDBObject("date", raw_data.getDate()).append("frame", raw_data.getFrame()));
				    gpsData.insert(doc);
			    }catch(Exception e){
						logger.info("Some thing went wrong with mongo!");
					}
				}
			}
		}
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