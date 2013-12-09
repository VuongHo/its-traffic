import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Timer implements Runnable{
	private static Logger logger = Logger.getLogger(Timer.class.getName());

	public Timer(){
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this, 0, 1000*30 , TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		logger.info("Scheduler ticks");
		CPU cpu = new CPU();
		cpu.start();
	}
}