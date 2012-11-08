import java.util.HashMap;

public class FlightHashTable 
{
	static RMHashtable m_itemHT = new RMHashtable();
	static HashMap<Integer, RMHashtable> recoveryMap = new HashMap();
 	
 	public static RMItem readData( int id, String key )
	{
		synchronized(m_itemHT)
		{
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	public static void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT)
		{
			logBeforeValue(id, key);
			m_itemHT.put(key, value);
		}
	}

	// Remove the item out of storage
	public static RMItem removeData(int id, String key)
	{
		synchronized(m_itemHT)
		{
			logBeforeValue(id, key);
			return (RMItem)m_itemHT.remove(key);
		}
	}
	
	public static void logBeforeValue(int id, String key)
	{
		RMHashtable table;
		if(recoveryMap.containsKey(id))
		{
			table = recoveryMap.get(id);
			if(!table.containsKey(key))
			{
				table.put(key, readData(id, key));
			}
		}
		else
		{
			table.put(key, readData(id, key));
		}
		recoveryMap.put(id, table);
	}
}