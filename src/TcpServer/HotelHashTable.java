package TcpServer;

import java.util.Enumeration;
import java.util.HashMap;

public class HotelHashTable {
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
		Hotel temp = (Hotel) readData(id, key);
		if (recoveryMap.containsKey(id)) {
			table = recoveryMap.get(id);
			if (!table.containsKey(key) && temp != null) {
				Hotel newHotel = new Hotel(temp.getLocation(), temp.getCount(), temp.getPrice());
				newHotel.setReserved(temp.getReserved());
				table.put(key, newHotel);
			}
			else if (temp == null){
				table.put(key, zero);
			}
		} else {
			table = new RMHashtable();
			if(temp != null)
			{
				Hotel newHotel = new Hotel(temp.getLocation(), temp.getCount(), temp.getPrice());
				newHotel.setReserved(temp.getReserved());
				table.put(key, newHotel);
			}
			else if (temp == null){
				table.put(key, zero);
			}
		}
		recoveryMap.put(id, table);
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