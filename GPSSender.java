import java.io.*;
import java.net.*;
import java.util.Scanner;

class GPSSender {
	// private static String LOCAL_BROADCAST_ADDRESS = "203.162.44.52";
	// private static int PORT = 180;
	private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	private static int PORT = 9876;

	public static void main(String args[]){
		boolean isRunning = true;
		Scanner scan = new Scanner(System.in);
		while(isRunning){
			System.out.println("waiting input (exit)");
			String line = scan.nextLine();
			if(line.equalsIgnoreCase("exit")){
				isRunning = false;
			}else{
				System.out.println("Starting.... ");
				try {
	    		byte[] receiveData = new byte[1024];
					String message = line;
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
			}
		}
	}
}
