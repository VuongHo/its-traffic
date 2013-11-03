import java.text.SimpleDateFormat;
import java.util.Date;

public class RawData{
	private String device_id;
	private float latitude;
	private float longitude;
	private float speed;
	private int satellite;
	private int lock;
	private Date trktime;
	private String date;
	private int frame;

	public RawData(String[] raw_data){
		this.device_id = raw_data[0];
		this.latitude = Float.parseFloat(raw_data[1]);
		this.longitude = Float.parseFloat(raw_data[2]);
		this.speed = Float.parseFloat(raw_data[3]);
		this.satellite = 0;
		this.lock = 0;
		this.trktime = today();
		this.date = dateTimeCurrent();
		this.frame = currentFrame();
	}

	public String getDeviceId(){
		return this.device_id;
	}
	public float getLatitude(){
		return this.latitude;
	}
	public float getLongitude(){
		return this.longitude;
	}
	public float getSpeed(){
		return this.speed;
	}
	public int getSatellite(){
		return this.satellite;
	}
	public int getLock(){
		return this.lock;
	}
	public Date getTrktime(){
		return this.trktime;
	}
	public String getDate(){
		return this.date;
	}
	public int getFrame(){
		return this.frame;
	}

	public String dateTimeCurrent(){
		SimpleDateFormat dateformat = new SimpleDateFormat ("yyyy-MM-dd");
		return dateformat.format(today());
	}

	public int currentFrame(){
		return (today().getHours())*4 + (int)((today().getMinutes())/15);
	}

	public Date today(){
		return new Date();
	}
}