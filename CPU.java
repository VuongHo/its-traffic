import com.mongodb.*;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class CPU implements Runnable {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static CPU cpu = new CPU();
	private DB db;
	private boolean isRunning;
	private Task currentTask;
	private Thread thread;
	private Queue<Task> runQueue = new ConcurrentLinkedQueue<Task>();

	private CPU() {
		if(MongoDB.check == false) MongoDB.openConnection();
		db = MongoDB.db;
		thread = new Thread(this);
	}
	
	public static CPU getInstance(){
		return cpu;
	}
	
	@Override
	public void run(){
		while(isRunning){
			if(currentTask != null){
				int cell_x = currentTask.cellX();	
				int cell_y = currentTask.cellY();
				String date = currentTask.getDate();
				int frame = currentTask.getFrame();
				float speed = currentTask.getSpeed();

				try{
					DBCollection segmentspeed_co = db.getCollection("segmentspeed");
					DBCollection segment_cell_co = db.getCollection("segment_cell_details");
					DBCollection segment_co = db.getCollection("segment");
					DBCollection street_co = db.getCollection("street");
					BasicDBObject query = new BasicDBObject("cell_x", cell_x).append("cell_y", cell_y);

					DBCursor cursor = segment_cell_co.find(query);
					try {
						while(cursor.hasNext()) {
							BasicDBObject segment_cell = (BasicDBObject) cursor.next();
					    if(currentTask.nodeBelongsToSegment(segment_cell)){
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

				}catch(Exception e){
					logger.info("Some thing went wrong with mongo!");
				}
				currentTask = null;
			}else{
				if(runQueue.isEmpty()){
					try{
						logger.info("dile");
						Thread.sleep(1000);
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}else{
					Task task = runQueue.poll();
					setCurrentTask(task);
				}
				
			}
		}
	}
	
	public void start(){
		thread.start();
		isRunning = true;
	}
	
	public void setCurrentTask(Task currentTask){
		this.currentTask = currentTask;
	}
	
	public void addTask(Task task) {
		runQueue.offer(task);
	}
	
}