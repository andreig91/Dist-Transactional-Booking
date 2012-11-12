package TcpServer;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class MwHashTable {
	static RMHashtable m_itemHT = new RMHashtable();
	static HashMap<Integer, RMHashtable> recoveryMap = new HashMap();
	static ReservedItem zero = new ReservedItem("-1","-1",-1,-1);

	public static RMItem readData(int id, String key) {
		synchronized (m_itemHT) {
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	public static void writeData(int id, String key, RMItem value) {
		synchronized (m_itemHT) {
			logBeforeValue(id, key);
			m_itemHT.put(key, value);
		}
	}

	// Remove the item out of storage
	public static RMItem removeData(int id, String key) {
		synchronized (m_itemHT) {
			logBeforeValue(id, key);
			return (RMItem) m_itemHT.remove(key);
		}
	}

	public static void logBeforeValue(int id, String key) {
		RMHashtable table;
		Customer temp = (Customer) readData(id, key);
		if (recoveryMap.containsKey(id)) {
			table = recoveryMap.get(id);
			if (!table.containsKey(key) && temp != null) {
				table.put(key, copyCustomer(temp));
			}
			else if (temp == null){
				table.put(key, zero);
			}
		} else {
			table = new RMHashtable();
			if(temp != null)
			{	
				table.put(key, copyCustomer(temp));
			}
			else if (temp == null){
				table.put(key, zero);
			}
		}
		recoveryMap.put(id, table);
	}
	public static Customer copyCustomer(Customer temp)
	{
		Customer newCustomer = new Customer(temp.getID());
		RMHashtable table = temp.getReservations();
		Enumeration<String> keys = table.keys();
		while(keys.hasMoreElements())
		{
			String aKey = keys.nextElement();
			ReservedItem item = temp.getReservedItem(aKey);
			ReservedItem newItem = new ReservedItem(item.getReservableItemKey(), item.getLocation(), item.getCount(), item.getPrice());
			newCustomer.putReservedItem(aKey, newItem);
		}
		return newCustomer;
	}

	public static void abort(int id) {
		RMHashtable table;
		if (recoveryMap.containsKey(id)) {
			table = recoveryMap.get(id);
			Enumeration<String> enumKey = table.keys();
			while (enumKey.hasMoreElements()) {
				String key = enumKey.nextElement();
				RMItem item = (RMItem) table.get(key);
				if(item.equals(zero)){
					removeData(id, key);
				}
				else
					writeData(id, key, (RMItem) table.get(key));
			}
		}
		removeRecoveryInfo(id);
	}

	public static boolean commit(int id) {
		return removeRecoveryInfo(id);
	}

	public static boolean removeRecoveryInfo(int id) {
		RMHashtable table;
		if (recoveryMap.containsKey(id)) {
			recoveryMap.remove(id);
			return true;
		}
		return false;
	}
}