
import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import java.util.Date;
import java.util.Calendar;

public class Memcache {
	private static Memcache INSTANCE;
	private MemCachedClient client = new MemCachedClient();
	static public Memcache getInstance(){
		if(INSTANCE == null){
			INSTANCE = new Memcache();
		}
		return INSTANCE;
	}

	String filePatch            = Constant.MEMCACHED_CONFIG_FILE;
  String HOST                 =  ReadProperty.getInstance().getValue(filePatch, Constant.HOST);
  String WEIGHTS              =  ReadProperty.getInstance().getValue(filePatch, Constant.WEIGHTS);
  String INIT_CONN            =  ReadProperty.getInstance().getValue(filePatch, Constant.INIT_CONN);
  String MIN_CONN             =  ReadProperty.getInstance().getValue(filePatch, Constant.MIN_CONN);
  String MAX_CONN             =  ReadProperty.getInstance().getValue(filePatch, Constant.MAX_CONN);
  String MAINT_SLEEP          =  ReadProperty.getInstance().getValue(filePatch, Constant.MAINT_SLEEP);
  String NAGLE                =  ReadProperty.getInstance().getValue(filePatch, Constant.NAGLE);
  String MAX_IDLE             =  ReadProperty.getInstance().getValue(filePatch, Constant.MAX_IDLE);
  String SOCKET_TO            =  ReadProperty.getInstance().getValue(filePatch, Constant.SOCKET_TO);
  String SOCKET_CONNECT_TO    =  ReadProperty.getInstance().getValue(filePatch, Constant.SOCKET_CONNECT_TO);

	private Memcache(){
		SockIOPool pool = SockIOPool.getInstance();       
    pool.setServers( HOST.split(",")); 
    pool.setWeights(getWeightsProperty(WEIGHTS) );                  // 3,3
    pool.setInitConn(Integer.parseInt(INIT_CONN)  );                // 5
    pool.setMinConn(Integer.parseInt(MIN_CONN) );                   // 5
    pool.setMaxConn(Integer.parseInt(MAX_CONN));                    // 250
    pool.setMaintSleep(Integer.parseInt(MAINT_SLEEP));              // 30
    pool.setNagle(Boolean.parseBoolean(NAGLE));                     // FALSE
    pool.setMaxIdle(Long.parseLong(MAX_IDLE)  );                    // 21600000
    pool.setSocketTO(Integer.parseInt(SOCKET_TO));                  // 3000
    pool.setSocketConnectTO(Integer.parseInt(SOCKET_CONNECT_TO) );  // 0
    pool.initialize();
	}

	private Integer[] getWeightsProperty(String weights) {
    String[] strWeights = WEIGHTS.split(",");
    Integer[] IntWeights = new Integer[strWeights.length];

    int i = 0;
    for(String strWeight : strWeights){
        IntWeights[i] = Integer.valueOf(strWeight);
        i++;
    }
    return IntWeights;
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