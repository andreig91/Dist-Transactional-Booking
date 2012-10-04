// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//

import java.util.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class server
{
	static ServerSocket serverSocket = null;
	static ObjectInputStream iis;
	static PrintStream os;
	static Socket clientSocket = null;

	public static void main(String args[]) 
	{	 
		try 
		{
			if(args.length == 1)
			{
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			}
			else
			{
				System.out.println("Need the port number as argument");
				System.exit(0);
			}
		} 
		catch (IOException e) 
		{
			System.out.println(e);
		}
		System.out.println("The server started. To stop it press <CTRL><C>.");

		while (true)
		{
			try 
			{
				clientSocket = serverSocket.accept();
				new ResourceManagerImpl(clientSocket).start();
			}
			catch (IOException e)
			{
				System.out.println(e);
			}	 
		}
	}
}
