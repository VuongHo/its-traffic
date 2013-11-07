import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class QueueTask{
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static QueueTask instance = null;
	private static int count = 0;
	private Queue<RawData> runQueue;
	protected QueueTask(){
		runQueue = new ConcurrentLinkedQueue<RawData>();
	}
	public static QueueTask getInstance(){
		if(instance == null){
			instance = new QueueTask();
		}
		return instance;
	}
	public void pushTask(RawData task) {
		runQueue.offer(task);
		count++;
	}
	public RawData popTask() {
		RawData task = runQueue.poll();
		return task;
	}
	public boolean isEmpty(){
		return runQueue.isEmpty();
	}
	public void removeAll(){
		runQueue = new ConcurrentLinkedQueue<RawData>();
	}
	public int queueCount(){
		return count;
	}
}