import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CPUTimer implements Runnable{
	private static Logger logger = Logger.getLogger(CPUTimer.class.getName());

	public CPUTimer(){
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this, 0, 1000*5 , TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		logger.info("CPU ticks");
		Scheduler.getInstance().run();
	}
}