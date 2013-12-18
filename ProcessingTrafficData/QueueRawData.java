import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.ArrayList;

public class QueueRawData{
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static QueueRawData instance = null;
	private Queue<ArrayList<RawData>> runQueue;
	private int num = 0;

	protected QueueRawData(){
		runQueue = new ConcurrentLinkedQueue<ArrayList<RawData>>();
	}
	public static QueueRawData getInstance(){
		if(instance == null){
			instance = new QueueRawData();
		}
		return instance;
	}
	public void pushTask(ArrayList<RawData> task) {
		if(num == 5) {runQueue = new ConcurrentLinkedQueue<ArrayList<RawData>>(); num = 0;}
		runQueue.offer(task);
		num++;
	}
	public ArrayList<RawData> popTask() {
		num--;
		ArrayList<RawData> task = runQueue.poll();
		return task;
	}
	public boolean isEmpty(){
		return runQueue.isEmpty();
	}
	public void removeAll(){
		runQueue = new ConcurrentLinkedQueue<ArrayList<RawData>>();
	}
	public int size(){
		return num;
	}
}