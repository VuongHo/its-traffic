import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Task {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private String device_id;
	private float latitude;
	private float longitude;
	private Float speed;
	private String date;
	private int frame;

	private double ROOT_LATITUDE = 10.609309;
	private double ROOT_LONGITUDE = 106.493811;
	private int PRIMARY_WAY = 0;
	private int SECONDARY_WAY = 1;
	private int TERTIARY = 2;
	private int OTHER_WAY = 3;
	private int cell_x;
	private int cell_y;
	private float[] width_of = new float[]{15,10,10,10};
	
	public Task(String device_id, Float latitude, Float longitude, Float speed, String date, int frame){
		this.device_id = device_id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.speed = speed;
		this.date = date;
		this.frame = frame;
		this.cell_x = (int) (Math.abs(latitude-ROOT_LATITUDE )/0.01);	
		this.cell_y = (int) (Math.abs(longitude-ROOT_LONGITUDE)/0.01);
	}

	public String deviceId(){
		return device_id;
	}

	public int cellX(){
		return cell_x;
	}

	public int cellY(){
		return cell_y;
	}

	public float nodeLat(){
		return latitude;
	}

	public float nodeLon(){
		return longitude;
	}

	public float getSpeed(){
		return speed;
	}

	public String getDate(){
		return date;
	}

	public int getFrame(){
		return frame;
	}
	
	public boolean nodeBelongsToSegment(JSONObject segment) throws Exception{
		
		float width;
		String street_type = (segment.getJSONObject("street")).getString("street_type");
		Double node_lat_s = (segment.getJSONObject("node_s")).getDouble("node_lat"); // Ay
		Double node_lon_s = (segment.getJSONObject("node_s")).getDouble("node_lon");
		Double node_lat_e = (segment.getJSONObject("node_e")).getDouble("node_lat"); // By
		Double node_lon_e = (segment.getJSONObject("node_e")).getDouble("node_lon");
		
		switch (street_type){
			case "primary":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			case "secondary":
				width = width_of[SECONDARY_WAY]/110000;
				break;
			case "tertiary":
				width = width_of[TERTIARY]/110000;
				break;
			case "motorway":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			case "motorway_link":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			case "trunk":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			case "trunk_link":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			case "primary_link":
				width = width_of[PRIMARY_WAY]/110000;
				break;
			default:
				width = width_of[OTHER_WAY]/110000;
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

		if (d_node_AB > width/2) return false;

		if ((longitude*a2 + latitude*b2 + c2)*(longitude*a4 + latitude*b4 + c4) > 0) return false;

		return true;
	}
	
}