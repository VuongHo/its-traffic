import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.Date;
public class QueueTask{
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static QueueTask instance = null;
	private static int dem1 = 0;
	private static int dem2 = 0;
	private static int count = 0;
	private static Date today = new Date();
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
		count++;
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
	public void increase(){
		dem1++;
	}
	public int getIncrease(){
		return dem1;
	}
	public void increase2(){
		dem2++;
	}
	public int getIncrease2(){
		return dem2;
	}

	public Date getdate(){
		return today;
	}

	public int queueCount(){
		return count;
	}
}