package TcpServer;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.*;

public class server 
{
  public static void main(String args[]) 
  {

    ServerSocket echoServer = null;
    ObjectInputStream iis;
	PrintStream os;
    Socket clientSocket = null;
    try 
    {
      if(args.length == 1)
      {
    	echoServer = new ServerSocket(Integer.parseInt(args[0]));
      }
    } 
    catch (IOException e) 
    {
      System.out.println(e);
    }

    System.out.println("The server started. To stop it press <CTRL><C>.");
    try 
    {
      clientSocket = echoServer.accept();
      iis = new ObjectInputStream(clientSocket.getInputStream());
      os = new PrintStream(clientSocket.getOutputStream());

      while (true) 
      {
        ArrayList<Object> array = new ArrayList<Object>();
        try
        {
			array = (ArrayList<Object>) iis.readObject();
		}
		catch(Exception e)
		{
	    }	
        if(array != null)
        {
			for(Object o : array)
			{
				System.out.println(o.toString());
			}
        }
      }
    }
    catch (IOException e)
    {
      System.out.println(e);
    }
  }
}

