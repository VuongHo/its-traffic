import com.mongodb.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

public class RawData{
	private String device_id;
	private Double latitude;
	private Double longitude;
	private Double speed;
	private Double reliability;
	private int satellite;
	private int type;
	private int lock;
	private String date;
	private int frame;

	private double ROOT_LATITUDE = 10.609309;
	private double ROOT_LONGITUDE = 106.493811;
	private double DISTANCE_MIN = 111200.00;
	private double DISTANCE_CELL = 0.01;

	private int PRIMARY_WAY = 0;
	private int SECONDARY_WAY = 1;
	private int TERTIARY = 2;
	private int OTHER_WAY = 3;

	private float[] width_of = new float[]{15,10,10,10};

	public RawData(BasicDBObject raw_gps_data){
		this.device_id = (String) raw_gps_data.get("device_id");
		this.latitude = (Double) raw_gps_data.get("latitude");
		this.longitude = (Double) raw_gps_data.get("longitude");
		this.speed = (Double) raw_gps_data.get("speed");
		this.reliability = (Double) raw_gps_data.get("reliability");
		this.satellite = (int) raw_gps_data.get("satellite");
		this.type = (int) raw_gps_data.get("type");
		this.lock = (int) raw_gps_data.get("lock");
		this.date = (String)((DBObject) raw_gps_data.get("date_key")).get("date");
		this.frame = (int)((DBObject) raw_gps_data.get("date_key")).get("frame");
	}

	public boolean nodeMatchSegment(SegmentCell segment){
		double width;
		String street_type = segment.getStreetType();
		Double node_lat_s = segment.getSNodeLat(); // Ay
		Double node_lon_s = segment.getSNodeLon();
		Double node_lat_e = segment.getENodeLat(); // By
		Double node_lon_e = segment.getENodeLon();

		// if(!((node_lat_s >= (this.latitude - 0.00015) && node_lon_s >= (this.longitude - 0.00015) &&
				  // node_lat_e <= (this.latitude + 0.00015) && node_lon_e <= (this.longitude + 0.00015)   ) || 
				 // (node_lat_s <= (this.latitude + 0.00015) && node_lon_s <= (this.longitude + 0.00015) &&
				  // node_lat_e >= (this.latitude - 0.00015) && node_lon_e >= (this.longitude - 0.00015)   ))) return false;
		
		switch (street_type){
			case "primary":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			case "secondary":
				width = width_of[SECONDARY_WAY]/DISTANCE_MIN;
				break;
			case "tertiary":
				width = width_of[TERTIARY]/DISTANCE_MIN;
				break;
			case "motorway":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			case "motorway_link":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			case "trunk":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			case "trunk_link":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			case "primary_link":
				width = width_of[PRIMARY_WAY]/DISTANCE_MIN;
				break;
			default:
				width = width_of[OTHER_WAY]/DISTANCE_MIN;
		}
		// AB
		Double a1 = -(node_lat_s - node_lat_e);
		Double b1 = 	node_lon_s - node_lon_e;
		Double c1 = -(a1*node_lon_s + b1*node_lat_s);
		// Qua A vtpt AB
		Double a2 = node_lon_s - node_lon_e;
		Double b2 = node_lat_s - node_lat_e;
		Double c2 = -(a2*node_lon_s + b2*node_lat_s);
		// Qua B vtpt AB
		Double a4 = node_lon_s - node_lon_e;
		Double b4 = node_lat_s - node_lat_e;
		Double c4 = -(a4*node_lon_e + b4*node_lat_e);

		Double d_node_AB = Math.abs(longitude*a1 + latitude*b1 + c1)/Math.sqrt(Math.pow(a1,2)+Math.pow(b1,2));

		if (Double.compare(d_node_AB, width/2) > 0) return false;

		if ((longitude*a2 + latitude*b2 + c2)*(longitude*a4 + latitude*b4 + c4) > 0) return false;

		return true;
	}

	public String getDeviceId(){
		return this.device_id;
	}

	public Double getLatitude(){
		return this.latitude;
	}

	public Double getLongitude(){
		return this.longitude;
	}

	public Double getSpeed(){
		return this.speed;
	}

	public int getSatellite(){
		return this.satellite;
	}

	public int getLock(){
		return this.lock;
	}

	public String getDate(){
		return this.date;
	}

	public int getFrame(){
		return this.frame;
	}

	public int getCellX(){
		return (int) (Math.abs(this.latitude-ROOT_LATITUDE )/DISTANCE_CELL);
	}

	public int getCellY(){
		return (int) (Math.abs(this.longitude-ROOT_LONGITUDE)/DISTANCE_CELL);
	}

	public String getKey(){
		return ""+this.device_id+""+this.longitude+""+this.latitude;
	}
}