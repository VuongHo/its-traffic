
public class Scheduler {
	private static Scheduler scheduler = new Scheduler();

	private Scheduler(){}
	
	public static Scheduler getInstance(){
		return scheduler;
	}
	
	public void schedule(){
		CPU cpu = new CPU();
		cpu.start();
	}

	
}
