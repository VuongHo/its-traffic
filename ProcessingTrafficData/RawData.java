import com.mongodb.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
	private Date date_time;

	private double ROOT_LATITUDE 	= Constant.ROOT_CELL_LATITUDE;
	private double ROOT_LONGITUDE = Constant.ROOT_CELL_LONGITUDE;
	private double DISTANCE_MIN 	= Constant.DISTANCE_ONE_DEGREE;
	private double DISTANCE_CELL 	= Constant.DISTANCE_CELL;

	private int PRIMARY_WAY 	= 0;
	private int SECONDARY_WAY = 1;
	private int TERTIARY 			= 2;
	private int OTHER_WAY 		= 3;

	private double ANPHA 	= Constant.ANPHA_OF_STREET;
	private String widths = Constant.WIDTH_OF_STREET;

	public Double[] getWidthOfStreet(String widths){
  	String[] strwidth_of = widths.split(",");
    Double[] dobwidth_of = new Double[strwidth_of.length];
    int i = 0;
    for(String width : strwidth_of){
        dobwidth_of[i] = Double.valueOf(width);
        i++;
    }
    return dobwidth_of;
  }

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
		this.date_time = (Date)raw_gps_data.get("date_time");
	}

	public RawData(String device_id,
								 Double latitude, Double longitude,
								 Double speed, Double reliability, int satellite, int type, int lock,
								 String date, int frame, Date date_time){
		this.device_id = device_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.reliability = reliability;
		this.satellite = satellite;
		this.type = type;
		this.lock = lock;
		this.date = date;
		this.frame = frame;
		this.date_time = date_time;

	}

	public int getType(){
		return this.type;
	}

	public Double getReliability(){
		return this.reliability;
	}


	public boolean nodeMatchSegment(SegmentCell segment){
		double width;
		Double[] width_of = getWidthOfStreet(this.widths);
		String street_type = segment.getStreetType();
		Double node_lat_s = segment.getSNodeLat(); // Ay
		Double node_lon_s = segment.getSNodeLon();
		Double node_lat_e = segment.getENodeLat(); // By
		Double node_lon_e = segment.getENodeLon();

		// if(!((node_lat_s >= (this.latitude - 0.00015) && node_lon_s >= (this.longitude - 0.00015) &&
				  // node_lat_e <= (this.latitude + 0.00015) && node_lon_e <= (this.longitude + 0.00015)   ) || 
				 // (node_lat_s <= (this.latitude + 0.00015) && node_lon_s <= (this.longitude + 0.00015) &&
				  // node_lat_e >= (this.latitude - 0.00015) && node_lon_e >= (this.longitude - 0.00015)   ))) return false;
		if(this.type == 1 || this.type == 2) ANPHA = 10.0;
		switch (street_type){
			case "primary":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "secondary":
				width = (width_of[SECONDARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "tertiary":
				width = (width_of[TERTIARY]+ANPHA)/DISTANCE_MIN;
				break;
			case "motorway":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "motorway_link":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "trunk":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "trunk_link":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			case "primary_link":
				width = (width_of[PRIMARY_WAY]+ANPHA)/DISTANCE_MIN;
				break;
			default:
				width = (width_of[OTHER_WAY]+ANPHA)/DISTANCE_MIN;
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

	public Double distance(SegmentCell segment){
		String street_type = segment.getStreetType();
		Double node_lat_s  = segment.getSNodeLat(); // Ay
		Double node_lon_s  = segment.getSNodeLon();
		Double node_lat_e  = segment.getENodeLat(); // By
		Double node_lon_e  = segment.getENodeLon();

		// AB
		Double a1 = -(node_lat_s - node_lat_e);
		Double b1 = 	node_lon_s - node_lon_e;
		Double c1 = -(a1*node_lon_s + b1*node_lat_s);

		Double d_node_AB = Math.abs(longitude*a1 + latitude*b1 + c1)/Math.sqrt(Math.pow(a1,2)+Math.pow(b1,2));

		return d_node_AB;
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

	public long getDateTime(){
		return this.date_time.getTime();
	}

	public String toString(){
		return "" + this.device_id + "," + this.latitude + "," + this.longitude + "," + this.speed + "";
	}

}