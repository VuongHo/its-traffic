import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class CPU implements Runnable {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static CPU cpu = new CPU();
	private boolean isRunning;
	private Task currentTask;
	private Thread thread;
	private Queue<Task> runQueue = new ConcurrentLinkedQueue<Task>();
	
	private CPU() {
		thread = new Thread(this);
	}
	
	public static CPU getInstance(){
		return cpu;
	}
	
	@Override
	public void run(){
		logger.info("CPU runs");
		while(isRunning){
			if(currentTask != null){
				currentTask.run();
				if (currentTask.completed()) currentTask = null;
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