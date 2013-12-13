

public class Main {
	public static void main(String[] args) {
		CpuRealTime.getInstance().start();
		CPU.getInstance().start();
		new Timer();
	}
}