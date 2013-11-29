import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueTask{
	private Queue<RawData> runQueue;
	private long count = 0;

	protected QueueTask(){
		runQueue = new ConcurrentLinkedQueue<RawData>();
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

	public long queueCount(){
		return count;
	}
}