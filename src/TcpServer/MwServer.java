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

public class MwServer {
	static ServerSocket serverSocket = null;
	static ObjectInputStream iis;
	static PrintStream os;
	static Socket clientSocket = null;
	static String rmF;
	static int rmFPort;
	static String rmC;
	static int rmCPort;
	static String rmH;
	static int rmHPort;

	public static void main(String args[]) {
		try {
			if (args.length == 7) {
				serverSocket = new ServerSocket(Integer.parseInt(args[0]));
				rmF = args[1];
				rmFPort = Integer.parseInt(args[2]);
				rmC = args[3];
				rmCPort = Integer.parseInt(args[4]);
				rmH = args[5];
				rmHPort = Integer.parseInt(args[6]);
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
				new MwImpl(clientSocket, rmF, rmFPort, rmC, rmCPort, rmH,
						rmHPort).start();
			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
