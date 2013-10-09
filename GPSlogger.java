/*
* The purpose of method is used to save the raw data, and automatic data processing
*/

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import java.net.*;
import com.mongodb.*;

public class GPSlogger {
	// private static String LOCAL_BROADCAST_ADDRESS = "203.162.44.52";
	// private static int PORT = 180;
	private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	private static int PORT = 9876;
	private UDPListener udpListener;

	public static void main(String args[]){
		GPSlogger main = new GPSlogger(PORT);
		main.startThreads();
	}

	public GPSlogger(int port){
		udpListener = new UDPListener(port);
	}

	public void startThreads(){
		udpListener.start();	
	}

	private class UDPListener extends Thread {
		int port;
		boolean run = true;

		public UDPListener (int port){
			this.port = port;
		}

		public synchronized boolean isRunning(){
			return run;
		}

		public synchronized void setRun(boolean is_running){
			run = is_running;
		}

		public void run() {
			System.out.println("Starting " + this.getClass().toString());
			try {
				DatagramSocket serverSocket = new DatagramSocket(port);
				serverSocket.setSoTimeout(9000);
				byte[] receiveData = new byte[1024];
				byte[] sendData = new byte[1024];
				while(isRunning()){
					try {
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						serverSocket.receive(receivePacket);
						String gps_data = new String(receivePacket.getData());
						System.out.println("RECEIVE: " + gps_data + " size: " + receivePacket.getLength() + " bytes" + " - " + receivePacket.getAddress());

						// Save data
						ReceiveRawData new_data = new ReceiveRawData();
						new_data.insertRawGpsData(gps_data.split(","));

						// Response result to client
			    	InetAddress IPAddress = receivePacket.getAddress();
			    	int port = receivePacket.getPort();
			    	String capitalizedSentence = gps_data.toUpperCase();
			    	sendData = capitalizedSentence.getBytes();
			    	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			    	serverSocket.send(sendPacket);
					}
					catch (SocketTimeoutException ex) {
						System.out.println("Receive timed out!");
						// ex.printStackTrace();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
				System.out.println("Done!");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class ReceiveRawData{

		public ReceiveRawData() {
			
		}

		public void insertRawGpsData(String[] data){
			if(data.length < 6) return ;
			DB db;
			String date="";
			String time="";
			int hour, minute, frameTemp;
			SimpleDateFormat dateformat = new SimpleDateFormat ("yyyy-MM-dd");
			SimpleDateFormat timeformat = new SimpleDateFormat ("HH:mm:ss");
			
			try {
				//Real Time
				Date today = new Date();
				hour = today.getHours();
				minute = today.getMinutes();
				frameTemp = hour*4 + (int)(minute/15);
				
				date=dateformat.format(today);
				time=timeformat.format(today);
				System.out.println("Date is " +date+" at "+ time);

				MongoClient mongoClient = new MongoClient("localhost", 27017 );
				db = mongoClient.getDB("hcm_traffic");
				DBCollection gpsData = db.getCollection("GPSdata");
				BasicDBObject doc = new BasicDBObject("device_id", data[0]).
																	append("latitude", Double.parseDouble(data[1])).
																	append("longitude", Double.parseDouble(data[2])).
																	append("speed", Double.parseDouble(data[3])).
																	append("reliability", Double.parseDouble(data[4])).
																	append("number_of_satellites", Double.parseDouble(data[5])).
																	append("current_time", new BasicDBObject("date", date).append("frame", frameTemp).append("time", time));
		    gpsData.insert(doc);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Some thing went wrong with mongo!");
			}
		}
	}
}