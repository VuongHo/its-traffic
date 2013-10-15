import java.io.*;
import java.net.*;
import java.util.Scanner;

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
		int count=0;
		int count2=0;
		int count10=0;
		int count20=0;
		int count0=0;
	 
		try {
	 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] values = line.split(",");
				
				deviceid = Integer.parseInt(values[0]);
				latPos = Double.parseDouble(values[1]);
				longPos = Double.parseDouble(values[2]);
				speed = Float.parseFloat(values[3]);
				date = values[6].substring(0, 10);
				hour = Integer.parseInt(values[6].substring(11, 13));
				min = Integer.parseInt(values[6].substring(14, 16));
				
				System.out.println("Starting.... ");
				try {
					System.out.println(line);
	    		byte[] receiveData = new byte[1024];
					String message = deviceid + "," + latPos + "," + longPos + "," + speed + ",0,0";
					DatagramSocket clientSocket = new DatagramSocket();
					InetAddress IPAddress = InetAddress.getByName(LOCAL_BROADCAST_ADDRESS);
					DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, IPAddress, PORT);
					clientSocket.send(sendPacket);
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

					// Receive data from server
			    clientSocket.receive(receivePacket);
			    String modifiedSentence = new String(receivePacket.getData());
			    System.out.println("FROM SERVER:" + modifiedSentence);
			    clientSocket.close();
				}catch(Exception e) {
					e.printStackTrace();
				}

		//			System.out.println(line);
		//			System.out.println(deviceid);
		//			System.out.println(latPos);
		//			System.out.println(longPos);
		//			System.out.println(speed);
		//			System.out.println(date);
		//			System.out.println(hour);
		//			System.out.println(min);
				count++;
				if (speed==0) count0++;
				else if (speed >0 && speed <10) count2++;
				else if (speed >=10 && speed <20) count10++;
				else count20++;
	 
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
	 
		System.out.println("Done");
		System.out.println("total of "+count);
		System.out.println("0 "+count0);
		System.out.println("0< <10 : "+count2);
		System.out.println("10=< <20 : "+count10);
		System.out.println(">=20: "+count20);
	
  }
}
