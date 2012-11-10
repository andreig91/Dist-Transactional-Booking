package TcpServer;

// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RmServer {
	static ServerSocket serverSocket = null;
	static ObjectInputStream iis;
	static PrintStream os;
	static Socket clientSocket = null;

	public static void main(String args[]) {
		try {
			if (args.length == 1) {
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
			} else {
				System.out.println("Need the port number as argument");
				System.exit(0);
			}
		} catch (IOException e) {
			System.out.println(e);
		}
		System.out.println("The server started. To stop it press <CTRL><C>.");

		while (true) {
			try {
				clientSocket = serverSocket.accept();
				new RmImpl(clientSocket).start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
