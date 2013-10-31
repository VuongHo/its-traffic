
import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import java.util.Date;
import java.util.Calendar;

public class Memcache {
	public static MemCachedClient client = new MemCachedClient();
	public static Memcache memCached = null;

	static {
		String[] address = {"127.0.0.1:11211"};
		Integer[] weights = {3};
		int initialConnections = 5;
		int minSpareConnections = 5;
		int maxSpareConnections = 250;
		int maxIdleTime = 1000 * 30 * 30; // 15 minutes
		long maintThreadSleep = 1000; // 1 seconds
		int socketTimeOut = 1000; // 1 seconds to block on reads
		int socketConnectTO = 0; // to block on initial connections.  If 0, then will use blocking connect 
		boolean nagleAlg = false; // turn off Nagle's algorithm on all sockets in pool

		SockIOPool pool = SockIOPool.getInstance();

		pool.setServers(address);
		pool.setWeights(weights);
		pool.setInitConn(initialConnections);
		pool.setMinConn(minSpareConnections);
		pool.setMaxConn(maxSpareConnections);
		pool.setMaxIdle(maxIdleTime);
		pool.setMaintSleep(maintThreadSleep);
		pool.setNagle(nagleAlg);
		pool.setSocketTO(socketTimeOut);
		pool.setSocketConnectTO(socketConnectTO);
		pool.initialize();
	}

	protected Memcache(){	}

	public static Memcache getInstance(){
		if(memCached == null) memCached = new Memcache();
		return memCached;
	}

	public boolean set(String key, Object value){
		return client.set(key, value);
	}

	public boolean add(String key, Object value, Date expiry){
		return client.add(key, value, expiry);
	}

	public boolean add(String key, Object value){
		return client.add(key, value);
	}

	public boolean replace(String key, Object value){
		return client.replace(key, value);
	}

	public boolean replace(String key, Object value, Date expiry){
		return client.replace(key, value, expiry);
	}

	public boolean delete(String key){
		return client.delete(key);
	}

	public Object get(String key){
		return client.get(key);
	}

	public Object gets(String key){
		return client.gets(key);
	}

	public boolean flushAll(){
		return client.flushAll();
	}

	public Date expiringTime(int minutes){
		Calendar later = Calendar.getInstance();
   	later.add(Calendar.MINUTE, minutes);
   	Date laterTime = later.getTime();
   	return laterTime;
	}
}