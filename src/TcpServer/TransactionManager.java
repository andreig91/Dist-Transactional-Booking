import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public class TransactionManager 
{
	
	static HashMap<Integer, Vector> map = new HashMap<Integer, Vector>();
	
	static int start()
	{
		int id = 1;
		while(map.containsKey(id))
		{
			id = new Random().nextInt() + 2;
		}
		return id;
	}
	
	static void enlist(int id, Object rm)
	{
		Vector aVector;
		if(map.containsKey(id))
		{
			aVector = map.get(id);
			aVector.add(rm);
		}
		else
		{
			aVector.add(rm);
		}
		map.put(id, aVector);
	}
	
	static void commit(int id)
	{
		Vector aVector;
		if(map.containsKey(id))
		{
			aVector = map.get(id);
			int size = aVector.size();
			for(int i=0; i<size; i++)
			{
				
			}
		}
	}
	
	static void abort (int id)
	{
		Vector aVector;
		if(map.containsKey(id))
		{
			aVector = map.get(id);
			int size = aVector.size();
			for(int i=0; i<size; i++)
			{
				
			}
		}
	}
}
