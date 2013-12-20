import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class GPSLogger {
	private static Logger logger = Logger.getLogger(CPU.class.getName());
	private static String LOCAL_BROADCAST_ADDRESS = "203.162.44.52";
	private static int PORT = 180;
	// private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	// private static int PORT = 9876;
	private UDPListener udpListener;

	public static void main(String args[]){
		CPU.getInstance(1).start();
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
						String[] raw_data = gps_data.split(",");
						if (raw_data.length == 9 || raw_data.length == 10) QueueTask.getInstance().pushTask(new RawData(raw_data));

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