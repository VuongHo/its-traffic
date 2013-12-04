

public class Main {
	public static void main(String[] args) {
		Memcache.getInstance().flushAll();
		new Timer();
	}
}