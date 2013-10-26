import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class QueueTask{
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static QueueTask instance = null;
	private Queue<Task> runQueue;
	protected QueueTask(){
		runQueue = new ConcurrentLinkedQueue<Task>();
	}
	public static QueueTask getInstance(){
		if(instance == null){
			instance = new QueueTask();
		}
		return instance;
	}
	public void pushTask(Task task) {
		runQueue.offer(task);
		logger.info("Queue size: "+runQueue.size());
	}
	public Task popTask() {
		Task task = runQueue.poll();
		return task;
	}
	public boolean isEmpty(){
		return runQueue.isEmpty();
	}
	public void removeAll(){
		runQueue = new ConcurrentLinkedQueue<Task>();
	}
}