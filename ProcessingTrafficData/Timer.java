import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Timer implements Runnable{
	private static Logger logger = Logger.getLogger(Timer.class.getName());

	private int TIMER    							 		 = Constant.TIMER;

	public Timer(){
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this, 0, 1000*TIMER , TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		// logger.info("Scheduler ticks");
		Scheduler.getInstance().schedule();
		
		// CPU cpu = new CPU();
		// cpu.start();

	}
}