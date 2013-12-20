import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.HashMap;

public class QueueTask{
	private Queue<HashMap<String, SegmentSpeed>> runQueue;
	private static QueueTask instance = null;
	private long count = 0;

	protected QueueTask(){
		runQueue = new ConcurrentLinkedQueue<HashMap<String, SegmentSpeed>>();
	}

	public static QueueTask getInstance(){
		if(instance == null){
			instance = new QueueTask();
		}
		return instance;
	}

	public void pushTask(HashMap<String, SegmentSpeed> task) {
		if(count == 2) {runQueue = new ConcurrentLinkedQueue<HashMap<String, SegmentSpeed>>(); count = 0;}
		runQueue.offer(task);
		count++;
	}

	public HashMap<String, SegmentSpeed> popTask() {
		HashMap<String, SegmentSpeed> task = runQueue.poll();
		count--;
		return task;
	}

	public boolean isEmpty(){
		return runQueue.isEmpty();
	}

	public void removeAll(){
		runQueue = new ConcurrentLinkedQueue<HashMap<String, SegmentSpeed>>();
	}

	public long queueCount(){
		return count;
	}
}