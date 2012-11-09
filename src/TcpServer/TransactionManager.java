import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import TcpRm.FlightHashTable;
import TcpRm.HotelHashTable;
import TcpRm.CarHashTable;

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
		map.put(id, new Vector());
		return id;
	}
	
	static void enlist(int id, String rm)
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
				String rm = (String) aVector.elementAt(i);
				if(rm.equals("Mw"))
				{
					MwHashTable.commit(id);
				}
				else if(rm.equals("Flight"))
				{
					FlightHashTable.commit(id);
				}
				else if(rm.equals("Hotel"))
				{
					HotelHashTable.commit(id);
				}
				else
				{
					CarHashTable.commit(id);
				}
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
				String rm = (String) aVector.elementAt(i);
				if(rm.equals("Mw"))
				{
					MwHashTable.abort(id);
				}
				else if(rm.equals("Flight"))
				{
					FlightHashTable.abort(id);
				}
				else if(rm.equals("Hotel"))
				{
					HotelHashTable.abort(id);
				}
				else
				{
					CarHashTable.abort(id);
				}
			}
		}
	}
}
