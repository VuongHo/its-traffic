import java.net.UnknownHostException;
import com.mongodb.*;

public class MongoDB {
	public static DB db;
	public static Boolean check = false;
	
	public static void openConnection(){
		MongoClient mongoClient;
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );
			db = mongoClient.getDB("hcm_traffic"); //ten database
			check = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}