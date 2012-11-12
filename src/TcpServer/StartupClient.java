package TcpServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

public class StartupClient 
{
	static String message = "blank";

	public static void main(String args[]) 
	{
		StartupClient obj = new StartupClient();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		String command = "";
		Vector arguments = new Vector();
		int Id = 1;
		int Cid;
		int transactionID;
		int flightNum;
		int flightPrice = 1;
		int flightSeats = 1;
		boolean Room;
		boolean Car;
		int price = 1;
		int numRooms = 1;
		int numCars = 1;
		String location;

		Socket clientSocket = null;
		DataInputStream is = null;
		ObjectOutputStream oos = null;
		ObjectInputStream iis = null;
		
		try 
		{
			clientSocket = new Socket((args[0]), Integer.parseInt(args[1]));
			oos = new ObjectOutputStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
			iis = new ObjectInputStream(clientSocket.getInputStream());
		} 
		catch (UnknownHostException e) {
			System.err.println("Don't know about host");
		} 
		catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to host");
		}
		if (clientSocket != null && oos != null && is != null) 
		{
		} 
		else 
		{
			System.out.println("failed at clientSocket != null && os != null && is != null");
			System.exit(0);
		}
		try
		{
			ArrayList<Object> array = new ArrayList<Object>();
			String start = "start";
			array.add(start);
			oos.writeObject(array);
			transactionID = is.readInt();
		} 
		catch (Exception e) 
		{
			System.out.println("EXCEPTION:");
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		
		for(int i = 0; i < Integer.parseInt(args[2]) ; i++)
		{
			try 
			{
				flightNum = i + 1;
				String method = "addFlight";
				ArrayList<Object> array = new ArrayList<Object>();
				array.add(method);
				array.add(Id);
				array.add(flightNum);
				array.add(flightSeats);
				array.add(flightPrice);
				oos.writeObject(array);
				if(!is.readBoolean())
				{
					System.out.println("Deadlock detected\n Transaction was ABORTED!");
					System.exit(0);
				}
				if (is.readBoolean()) 
				{
				} 
				else 
				{
					System.out.println("Flights could not be added");
					System.exit(0);
				}
			} 
			catch (Exception e) {
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
			
			try 
			{
				location = Integer.toString(i);
				String method = "addCar";
				ArrayList<Object> array = new ArrayList<Object>();
				array.add(method);
				array.add(Id);
				array.add(location);
				array.add(numCars);
				array.add(price);
				oos.writeObject(array);
				
				if(!is.readBoolean())
				{
					System.out.println("Deadlock detected\n Transaction was ABORTED!");
					System.exit(0);
				}
				
				if (is.readBoolean()) 
				{
				} 
				else 
				{
					System.out.println("Cars could not be added");
					System.exit(0);
				}

			} 
			catch (Exception e) 
			{
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
			
			try 
			{
				location = Integer.toString(i);
				String method = "addRoom";
				ArrayList<Object> array = new ArrayList<Object>();
				array.add(method);
				array.add(Id);
				array.add(location);
				array.add(numRooms);
				array.add(price);
				oos.writeObject(array);
				
				if(!is.readBoolean())
				{
					System.out.println("Deadlock detected\n Transaction was ABORTED!");
					System.exit(0);
				}
				if (is.readBoolean()) 
				{
				} 
				else 
				{
					System.out.println("Rooms could not be added");
					System.exit(0);
				}
			} 
			catch (Exception e) 
			{
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
			
		}
		
		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			try 
			{
				Cid = i + 1;
				String method = "newCustomerId";
				ArrayList<Object> array = new ArrayList<Object>();
				array.add(method);
				array.add(Id);
				array.add(Cid);
				oos.writeObject(array);
				
				if(!is.readBoolean())
				{
					System.out.println("Deadlock detected\n Transaction was ABORTED!");
					System.exit(0);
				}
				if (is.readBoolean()) 
				{
				} 
				else 
				{
					System.out.println("Customer could not be created");
					System.exit(0);
				}

			} catch (Exception e) {
				System.out.println("EXCEPTION:");
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
		}
		
		try 
		{
			ArrayList<Object> array = new ArrayList<Object>();
			String start = "commit";
			array.add(start);
			oos.writeObject(array);

			if (is.readBoolean()) 
			{
				System.out.println("commit successful");
			} 
			else 
			{
				System.out.println("commit not successful");
				System.exit(0);
			}

		} catch (Exception e) {
			System.out.println("EXCEPTION:");
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
}