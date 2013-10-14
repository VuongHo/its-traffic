import com.mongodb.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Polygon;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Task {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private String device_id;
	private Double latitude;
	private Double longitude;
	private Float speed;
	private String date;
	private int frame;

	private static double ROOT_LATITUDE = 10.609309;
	private static double ROOT_LONGITUDE = 106.493811;
	private static int PRIMARY_WAY = 0;
	private static int SECONDARY_WAY = 1;
	private static int TERTIARY = 2;
	private static int OTHER_WAY = 3;
	private static float[] width_of = new float[]{15,10,8,5};
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
		logger.info("Task runs");
		DB db;
		int cell_x = (int) (Math.abs(latitude-ROOT_LATITUDE )/0.01);	
		int cell_y = (int) (Math.abs(longitude-ROOT_LONGITUDE)/0.01);

		try{
			MongoClient mongoClient = new MongoClient("localhost", 27017 );
			db = mongoClient.getDB("hcm_traffic");
			DBCollection segmentspeed_co = db.getCollection("segmentspeed");
			DBCollection segment_cell_co = db.getCollection("segment_cell");
			DBCollection segment_co = db.getCollection("segment");
			DBCollection street_co = db.getCollection("street");
			BasicDBObject query = new BasicDBObject("cell_x", cell_x).append("cell_y", cell_y);

			DBCursor cursor = segment_cell_co.find(query);
			try {
				while(cursor.hasNext()) {
					BasicDBObject segment_cell = (BasicDBObject) cursor.next();
			    if(nodeBelongsToSegment(segment_cell)){
			    	query = new BasicDBObject("date", date).append("frame", frame).
			    																					append("segment_id", segment_cell.getInt("segment_id")).
			    																					append("cell_id", segment_cell.getInt("cell_id"));
			    	DBObject segment_speed = segmentspeed_co.findOne(query);

			    	if(segment_speed != null){
			    		double old_speed = (double)(segment_speed.get("speed"));
			    		int sum = (int)(segment_speed.get("sum")) + 1;
			    		segment_speed.put("speed", (float)((speed+old_speed)/sum));
			    		segment_speed.put("sum", sum);
			    		segmentspeed_co.save(segment_speed);
			    		logger.info("----UPDATE--------"+ segment_cell.get("segment_id") + " " + segment_cell.get("cell_id"));
			    	}else{
			    		query = new BasicDBObject("segment_id", segment_cell.getInt("segment_id")).
								    										append("cell_id", segment_cell.getInt("cell_id")).
								    										append("cell_x", cell_x).
								    										append("cell_y", cell_y).
								    										append("street_id", ((DBObject)segment_cell.get("street")).get("street_id")).
								    										append("speed", speed).
								    										append("sum", 1).
								    										append("date", date).
								    										append("frame", frame);
							segmentspeed_co.insert(query);
							logger.info("----INSERT--------"+ segment_cell.get("segment_id") + " " + segment_cell.get("cell_id"));
						}
					}

				}
			} finally {
				cursor.close();
			}

		}catch(UnknownHostException e){
			e.printStackTrace();
		}catch(Exception e){
			System.err.println("Some thing went wrong with mongo!");
		}

		this.status = true;
		System.out.println("oke");
	}

	public boolean nodeBelongsToSegment(BasicDBObject segment){
		
		float width;
		String street_type = (String) ((DBObject)segment.get("street")).get("street_type");
		Double node_lat_s = (Double)((DBObject)segment.get("node_s")).get("node_lat"); // Ay
		Double node_lon_s = (Double)((DBObject)segment.get("node_s")).get("node_lon");
		Double node_lat_e = (Double)((DBObject)segment.get("node_e")).get("node_lat"); // By
		Double node_lon_e = (Double)((DBObject)segment.get("node_e")).get("node_lon");
		
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
		// Song Song AB cach khoang width/2
		Double a3 = a1;
		Double b3 = b1;
		Double c3 = width*Math.sqrt(a3*a3+b3*b3)/2 - Math.abs(a3*node_lon_s+b3*node_lat_s);

		Double a5 = a1;
		Double b5 = b1;
		Double c5 = -c3;

		// DCEF
		// Nghiem D
		// Double d_D = a2*b3 - a3*b2;
		// Double d_Dx = c2*b3 - c3*b2;
		// Double d_Dy = a2*c3 - a3*c2;
		Double dX = (c2*b3 - c3*b2)/(a2*b3 - a3*b2);
		Double dY = (a2*c3 - a3*c2)/(a2*b3 - a3*b2);
		// Nghiem F
		Double fX = (c2*b5 - c5*b2)/(a2*b5 - a5*b2);
		Double fY = (a2*c5 - a5*c2)/(a2*b5 - a5*b2);
		// Nghiem C
		// Double c_D = a4*b3 - a3*b4;
		// Double c_Dx = c4*b3 - c3*b4;
		// Double c_Dy = a4*c3 - a3*c4;
		Double cX = (c4*b3 - c3*b4)/(a4*b3 - a3*b4);
		Double cY = (a4*c3 - a3*c4)/(a4*b3 - a3*b4);
		// Nghiem E
		Double eX = (c4*b5 - c5*b4)/(a4*b5 - a5*b4);
		Double eY = (a4*c5 - a5*c4)/(a4*b5 - a5*b4);

		int[] xpoints = new int[]{(int)(dX*100000),(int)(cX*100000),(int)(eX*100000),(int)(fX*100000)};
		int[] ypoints = new int[]{(int)(dY*100000),(int)(cY*100000),(int)(eY*100000),(int)(fY*100000)};
		int npoints = 4;
		Polygon polygon = new Polygon(xpoints, ypoints, npoints);
		logger.info("------------"+ ((DBObject)segment.get("street")).get("street_type") + " " + width);
		logger.info("------------"+ dX + " - " + cX + " - " + eX + " - " + fX + " " + width);
		logger.info("------------"+ dY + " - " + cY + " - " + eY + " - " + fY + " " + width);
		logger.info("------------"+ longitude + "  " + latitude);
		return polygon.contains(longitude*100000, latitude*100000);
	}
	
	public boolean completed(){
		return this.status;
	}
	
}