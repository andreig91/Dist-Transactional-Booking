package TcpServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

public class clientC 
{
	
	public static long avgRespTime = 0;
	public static long totalAvgResp = 0;
	
	public static synchronized void incrRespTime(long time)
	{
		avgRespTime += time;
	}
	
	public static void main(String args[]) throws InterruptedException
	{
		Thread[] threads = new Thread[Integer.parseInt(args[3])];
		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			threads[i] = new clientAuto(args,i);
		}
		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			threads[i].start();
		}
		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			threads[i].join();
		}
		System.out.println("Average response time accross all threads: " + (avgRespTime / (float)Integer.parseInt(args[3])) + "ms");
		totalAvgResp += (avgRespTime / (float)Integer.parseInt(args[3]));
		avgRespTime = 0;
		
		/*String command = "shutdown";
		Vector arguments = new Vector();
		int Id, Cid, transactionID;
		int flightNum;
		int flightPrice;
		int flightSeats;
		boolean Room;
		boolean Car;
		int price;
		int numRooms;
		int numCars;
		String location;
		int numberOfTransactions = 0;
		Socket clientSocket = null;
		DataInputStream is = null;
		ObjectOutputStream oos = null;
		ObjectInputStream iis = null;
		BufferedReader in = null;
		long totalResponseTime = 0;
		long totalAverage = 0;
		try {
			clientSocket = new Socket((args[0]), Integer.parseInt(args[1]));
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
			iis = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to host");
		}
		if (clientSocket != null && oos != null && is != null) {
		} else {
			System.out
			.println("failed at clientSocket != null && os != null && is != null");
			System.exit(0);
		}*/
	}
}
