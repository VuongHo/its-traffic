import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

class GPSSender {
	// private static String LOCAL_BROADCAST_ADDRESS = "203.162.44.52";
	// private static int PORT = 180;
	private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	private static int PORT = 9876;

	public static void main(String args[]){
		GPSSender obj = new GPSSender();
		obj.run();
	}

	public void run() {
 
		String csvFile = "processedData/Monday.txt";
		BufferedReader br = null;
		String line = "";
		int deviceid;
		double latPos;
		double longPos;
		float speed;
		String date;
		int hour, min;
		int frame;

	 	SimpleDateFormat timeformat = new SimpleDateFormat ("HH:mm:ss");
		try {
			while(true){
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					String[] values = line.split(",");
					deviceid = Integer.parseInt(values[0]);
					latPos = Double.parseDouble(values[1]);
					longPos = Double.parseDouble(values[2]);
					speed = Float.parseFloat(values[3]);
					date = values[6].substring(0, 10);
					hour = Integer.parseInt(values[6].substring(11, 13));
					min = Integer.parseInt(values[6].substring(14, 16));

					Date today = new Date();
					String time=timeformat.format(today);
					String current_time = values[6].substring(11, 19);
					
		 			if(time.equals(current_time)){
		 				String message = deviceid + "," + latPos + "," + longPos + "," + speed + ",0,0";
		 				UDPSender sender = new UDPSender(PORT, message);
            sender.start();
		 			}else{
		 				continue;
		 			}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
  }

private class UDPSender extends Thread{
    int port;
    String message;
    public UDPSender(int port, String message)
    {
        this.port = port;
        this.message = message;
    }
    public void run()
    {
        System.out.println("Starting "+this.getClass().toString());
        try
        {
            byte[] receiveData = new byte[1024];
						DatagramSocket clientSocket = new DatagramSocket();
						InetAddress IPAddress = InetAddress.getByName(LOCAL_BROADCAST_ADDRESS);
						DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, IPAddress, port);
						clientSocket.send(sendPacket);
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

						// Receive data from server
				    clientSocket.receive(receivePacket);
				    String modifiedSentence = new String(receivePacket.getData());
				    System.out.println("FROM SERVER:" + modifiedSentence);
				    clientSocket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
}
