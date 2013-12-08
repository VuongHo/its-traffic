import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

public class GpsSegmentData{
	private static GpsSegmentData instance = null;
	private HashMap<String, ArrayList<SegmentCell>> seg_cells;
	private Date datetime;
	public GpsSegmentData(){
		seg_cells = new HashMap<>();
		datetime = new Date();
	}
	public static GpsSegmentData getInstance(){
		if(instance == null){
			instance = new GpsSegmentData();
		}
		return instance;
	}

	public synchronized HashMap<String, ArrayList<SegmentCell>> getSegmentCells(){
		return seg_cells;
	}

	public synchronized void setSegmentCells(HashMap<String, ArrayList<SegmentCell>> seg_cells){
		this.seg_cells = seg_cells;
	}

	public synchronized Date getDateTime(){
		return datetime;
	}

	public synchronized void setDateTime(Date date){
		this.datetime = date;
	}

}