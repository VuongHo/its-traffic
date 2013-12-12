
public class SegmentSpeed {
	private int segment_id;
	private int cell_id;
	private int cell_x;
	private int cell_y;
	private int street_id;
	private double speed;
	private String date;
	private int frame;
	private int sum;

	public SegmentSpeed(int segment_id, int cell_id, int cell_x, int cell_y, 
											int street_id, double speed, String date, int frame){
		this.segment_id = segment_id;
		this.cell_id = cell_id;
		this.cell_x = cell_x;
		this.cell_y = cell_y;
		this.street_id = street_id;
		this.speed = speed;
		this.date = date;
		this.frame = frame;
		this.sum = 1;
	}

	public int getSum(){
		return sum;
	}

	public void setSum(int sum){
		this.sum = sum;
	}

	public void setSpeed(double speed){
		this.speed = speed;
	}

	public int getSegmentId(){
		return this.segment_id;
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
	public int getStreetId(){
		return this.street_id;
	} 
	public double getSpeed(){
		return this.speed;
	} 
	public String getDate(){
		return this.date;
	} 
	public int getFrame(){
		return this.frame;
	}


}