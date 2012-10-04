
public class MwHashTable {
 static RMHashtable m_itemHT = new RMHashtable();
 	
 	public static RMItem readData( int id, String key )
	{
		synchronized(m_itemHT){
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	public static void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT){
			m_itemHT.put(key, value);
		}
	}

	// Remove the item out of storage
	public static RMItem removeData(int id, String key){
		synchronized(m_itemHT){
			return (RMItem)m_itemHT.remove(key);
		}
	}
}
