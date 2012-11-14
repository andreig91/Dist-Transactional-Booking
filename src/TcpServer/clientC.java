package TcpServer;

public class clientC {
	public static void main(String args[]) throws InterruptedException
	{
		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			new clientAuto(args,i).start();
			//Thread.sleep(4000);
		}
	}
}
