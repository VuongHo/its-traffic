import com.mongodb.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.UnknownHostException;

public class ReceiveRawData{
	public ReceiveRawData() {
		
	}

	public void insertRawGpsData(String[] data){
		if(data.length < 6) return ;
		DB db;
		String date="";
		String time="";
		int hour, minute, frameTemp;
		SimpleDateFormat dateformat = new SimpleDateFormat ("yyyy-MM-dd");
		SimpleDateFormat timeformat = new SimpleDateFormat ("HH:mm:ss");
		
		try {
			//Real Time
			Date today = new Date();
			hour = today.getHours();
			minute = today.getMinutes();
			frameTemp = hour*4 + (int)(minute/15);
			
			date=dateformat.format(today);
			time=timeformat.format(today);
			System.out.println("Date is " +date+" at "+ time);

			CPU.getInstance().addTask(new Task( data[0], 
																					Float.parseFloat(data[1]),
																					Float.parseFloat(data[2]),
																					Float.parseFloat(data[3]),
																					date,
																					frameTemp));

			// MongoClient mongoClient = new MongoClient("localhost", 27017 );
			// db = mongoClient.getDB("hcm_traffic");
			// DBCollection gpsData = db.getCollection("GPSdata");
			// BasicDBObject doc = new BasicDBObject("device_id", data[0]).
			// 													append("latitude", Float.parseFloat(data[1])).
			// 													append("longitude", Float.parseFloat(data[2])).
			// 													append("speed", Float.parseFloat(data[3])).
			// 													append("reliability", Float.parseFloat(data[4])).
			// 													append("number_of_satellites", Float.parseFloat(data[5])).
			// 													append("current_time", new BasicDBObject("date", date).append("frame", frameTemp).append("time", time));
	  //   gpsData.insert(doc);
		} catch (Exception e) {
			System.err.println("Some thing went wrong with mongo!");
		}
	}
}