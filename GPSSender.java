import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.LinkedList;
import java.util.List;

class GPSSender {
	// private static String LOCAL_BROADCAST_ADDRESS = "172.28.10.96";
	// private static int PORT = 170;
	private static String LOCAL_BROADCAST_ADDRESS = "127.0.0.1";
	private static int PORT = 9876;
	private boolean isRunning = true;

	public static void main(String args[]){
		GPSSender obj = new GPSSender();
		obj.run();
	}

	public void run(){
		System.out.println("Enter numbers of device your want to run");
    Scanner scan = new Scanner(System.in);
    while(isRunning)
    {
      System.out.println("waiting input (exit)");
      String line = scan.nextLine();
      if(line.equalsIgnoreCase("exit"))
      {
        System.out.println("Shutting down server...");
        isRunning = false;
      }
      else
      {
       	try{
      		int numberOfDevice = Integer.parseInt(line);
      		String row = "";
      		String csvFile = getCsvFile();
      		List<Integer> listDevice = new LinkedList<Integer>();
      		int count = 0;
      		BufferedReader br = new BufferedReader(new FileReader(csvFile));
					while ((row = br.readLine()) != null){
						String[] values = row.split(",");
						int deviceid = Integer.parseInt(values[0]);
						System.out.println("Device"+checkDevice(listDevice, deviceid)+" is running!");
						if (checkDevice(listDevice, deviceid) == false || listDevice != null){
							listDevice.add(deviceid);
							// TODO
							UserInputThread newDevice = new UserInputThread(deviceid, csvFile);
							newDevice.start();
							count++;
							System.out.println("Device"+count+" is running!");
							if(count == numberOfDevice) break;
						}
					} 
      	} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
      }
    }
	}

  public boolean checkDevice(List<Integer> listDevice, int deviceid){
  	int size = listDevice.size();
  	for(int i = 0; i < size; i++){
  		if(listDevice.get(i) == deviceid) return false;
  	}
  	return true;
  }

  public String getCsvFile(){
  	Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
		int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
		String csvFile = "";
		switch(currentDayOfWeek){
			case 1:
				csvFile = "processedData/Sunday.txt";
				break;
			case 2:
				csvFile = "processedData/Monday.txt";
				break;
			case 3:
				csvFile = "processedData/Tuesday.txt";
				break;
			case 4:
				csvFile = "processedData/Wednesday.txt";
				break;
			case 5:
				csvFile = "processedData/Thursday.txt";
				break;
			case 6:
				csvFile = "processedData/Friday.txt";
				break;
			case 7:
				csvFile = "processedData/Saturday.txt";
				break;
		}
		return csvFile;
  }

  private class UserInputThread extends Thread
	{
	    int deviceid;
	    String csvFile;
	    boolean isrunning = true;
	    public UserInputThread(int deviceid, String csvFile)
	    {
	        this.deviceid = deviceid;
	        this.csvFile = csvFile;
	    }
	    public void run()
	    {
        BufferedReader br = null;
				String line = "";
				int device_id;
				double latPos;
				double longPos;
				float speed;
				String date;
				int hour, min;
				int frame;

			 	SimpleDateFormat timeformat = new SimpleDateFormat ("HH:mm:ss");
				try {
					while(isrunning){
						br = new BufferedReader(new FileReader(csvFile));
						while ((line = br.readLine()) != null) {
							String[] values = line.split(",");
							device_id = Integer.parseInt(values[0]);
							latPos = Double.parseDouble(values[1]);
							longPos = Double.parseDouble(values[2]);
							speed = Float.parseFloat(values[3]);
							date = values[6].substring(0, 10);
							hour = Integer.parseInt(values[6].substring(11, 13));
							min = Integer.parseInt(values[6].substring(14, 16));

							Date today = new Date();
							String time=timeformat.format(today);
							String current_time = values[6].substring(11, 19);
							
				 			if(time.equals(current_time) && (deviceid == deviceid)) {
				 				String message = device_id + "," + latPos + "," + longPos + "," + speed + ",0,0";
				 				UDPSender sender = new UDPSender(PORT, message);
		            sender.start();
		            try{
									Thread.sleep(1000);
								} catch (InterruptedException e){
									e.printStackTrace();
								}
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
							clientSocket.setSoTimeout(2000);
							InetAddress IPAddress = InetAddress.getByName(LOCAL_BROADCAST_ADDRESS);
							DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.getBytes().length, IPAddress, port);
							clientSocket.send(sendPacket);
							DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

							// Receive data from server
					    clientSocket.receive(receivePacket);
					    String modifiedSentence = new String(receivePacket.getData());
					    System.out.println("FROM SERVER:" + modifiedSentence);
					    clientSocket.close();
	        }catch (SocketTimeoutException ex) {
						System.out.println("Receive timed out!");
						// ex.printStackTrace();
					}
	        catch(Exception e)
	        {
	            e.printStackTrace();
	        }
	    }
	}
}
