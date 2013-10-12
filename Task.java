import com.mongodb.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.UnknownHostException;

public class Task {
	private String device_id;
	private Double latitude;
	private Double longitude;
	private Float speed;
	private String date;
	private int frame;
	private boolean status;
	
	public Task(String device_id, Double latitude, Double longitude, Float speed, String date, int frame){
		this.device_id = device_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.date = date;
		this.frame = frame;
		this.status = false;
	}
	
	public void run(){
		this.status = true;
		System.out.println("oke");
	}
	
	public boolean completed(){
		return this.status;
	}
	
}