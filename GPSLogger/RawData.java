import java.text.SimpleDateFormat;
import java.util.Date;

public class RawData{
	private String device_id;
	private double latitude;
	private double longitude;
	private double speed;
	private double reliability;
	private int satellite;
	private int type;
	private int lock;
	private String datetime;

	public RawData(String[] raw_data){
		this.device_id = raw_data[0];
		this.latitude  = Double.parseDouble(raw_data[1]);
		this.longitude = Double.parseDouble(raw_data[2]);
		this.speed     = Double.parseDouble(raw_data[3]);
		this.reliability = Double.parseDouble(raw_data[4]);
		this.satellite = Integer.parseInt(raw_data[5]);
		this.type      = Integer.parseInt(raw_data[6]);
		this.lock      = Integer.parseInt(raw_data[7]);
		this.datetime  = raw_data[8];
	}

	public String getDeviceId(){
		return this.device_id;
	}
	public double getLatitude(){
		return this.latitude;
	}
	public double getLongitude(){
		return this.longitude;
	}
	public double getSpeed(){
		return this.speed;
	}
	public double getReliability(){
		return this.reliability;
	}
	public int getSatellite(){
		return this.satellite;
	}
	public int getType(){
		return this.type;
	}
	public int getLock(){
		return this.lock;
	}
	public String getDateTime(){
		return this.datetime;
	}
}