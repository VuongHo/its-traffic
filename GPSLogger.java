/*
* The purpose of method is used to save the raw data, and automatic data processing
*/

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class GPSLogger {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	// private static String LOCAL_BROADCAST_ADDRESS = "203.162.44.52";
	// private static int PORT = 180;
	private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	private static int PORT = 9876;
	private UDPListener udpListener;

	public static void main(String args[]){
		Memcache.getInstance().flushAll();
		CPU.getInstance(1).start();
		CPU.getInstance(2).start();
		CPU.getInstance(3).start();
		CPU.getInstance(4).start();
		GPSLogger main = new GPSLogger(PORT);
		main.startThreads();
	}

	public GPSLogger(int port){
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
			logger.info("Starting... " + this.getClass().toString());
			try {
				DatagramSocket serverSocket = new DatagramSocket(port);
				serverSocket.setSoTimeout(2000);
				while(isRunning()){
					try {
						byte[] receiveData = new byte[1024];
						byte[] sendData = new byte[1024];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						serverSocket.receive(receivePacket);
						String gps_data = new String(receivePacket.getData());
						logger.info("RECEIVE: " + gps_data + " size: " + receivePacket.getLength() + " bytes" + " - " + receivePacket.getAddress());

						// Save data
						ReceiveRawData new_data = new ReceiveRawData();
						new_data.insertRawGpsData(gps_data.split(","));

						// Response result to client
			    	// InetAddress IPAddress = receivePacket.getAddress();
			    	// int port = receivePacket.getPort();
			    	// String capitalizedSentence = gps_data.toUpperCase();
			    	// sendData = capitalizedSentence.getBytes();
			    	// DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
			    	// serverSocket.send(sendPacket);
					}
					catch (SocketTimeoutException ex) {
						// ex.printStackTrace();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
				logger.info("Exit!");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}