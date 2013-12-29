import com.mongodb.*;

public class SegmentCell {
	private int segment_id;
	private double s_node_lat;
	private double s_node_lon;
	private double e_node_lat;
	private double e_node_lon;
	private int cell_id;
	private int cell_x;
	private int cell_y;
	private String street_type;
	private int street_id;

	public SegmentCell(){}

	public SegmentCell(int segment_id, double s_node_lat, double s_node_lon, double e_node_lat, double e_node_lon,
										 int cell_id, int cell_x, int cell_y,
										 String street_type, int street_id){
		this.segment_id = segment_id;
		this.s_node_lat = s_node_lat;
		this.s_node_lon = s_node_lon;
		this.e_node_lat = e_node_lat;
		this.e_node_lon = e_node_lon;
		this.cell_id = cell_id;
		this.cell_x = cell_x;
		this.cell_y = cell_y;
		this.street_type = street_type;
		this.street_id = street_id;
	}

	public SegmentCell(BasicDBObject segment_cell){
		this.segment_id = segment_cell.getInt("segment_id");
		this.s_node_lat = (Double)((DBObject)segment_cell.get("node_s")).get("node_lat");
		this.s_node_lon = (Double)((DBObject)segment_cell.get("node_s")).get("node_lon");
		this.e_node_lat = (Double)((DBObject)segment_cell.get("node_e")).get("node_lat");
		this.e_node_lon = (Double)((DBObject)segment_cell.get("node_e")).get("node_lon");
		this.cell_id = segment_cell.getInt("cell_id");
		this.cell_x = segment_cell.getInt("cell_x");
		this.cell_y = segment_cell.getInt("cell_y");
		this.street_type = (String)((DBObject)segment_cell.get("street")).get("street_type");
		this.street_id = Integer.parseInt((String)((DBObject)segment_cell.get("street")).get("street_id"));
	}

	public int getSegmentId(){
		return this.segment_id;
	}

	public double getSNodeLat(){
		return this.s_node_lat;
	}

	public double getSNodeLon(){
		return this.s_node_lon;
	}

	public double getENodeLat(){
		return this.e_node_lat;
	}

	public double getENodeLon(){
		return this.e_node_lon;
	}

	public int getCellId(){
		return this.cell_id;
	}

	public int getCellX(){
		return this.cell_x;
	}

	public int getCellY(){
		return this.cell_y;
	}

	public String getStreetType(){
		return this.street_type;
	}

	public int getStreetId(){
		return this.street_id;
	}
}