package TcpServer;

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager {

	static ConcurrentHashMap<Integer, Vector> map = new ConcurrentHashMap<Integer, Vector>();

	static int start() {
		int id = 1;
		while (map.containsKey(id) || id == 1 ) {
			id = new Random().nextInt(10000) + 1;
		}
		map.put(id, new Vector());
		return id;
	}

	static boolean transactionsLeft()
	{
		return 	map.keySet().size() > 0;
	}
	static void enlist(int id, String rm) {
		Vector aVector;
		if (map.containsKey(id)) {
			aVector = map.get(id);
			aVector.add(rm);
		} else {
			aVector = new Vector();
			aVector.add(rm);
		}
		map.put(id, aVector);
	}

	static boolean commit(int id) {
		Vector aVector;
		if (map.containsKey(id)) {
			aVector = map.get(id);
			int size = aVector.size();
	//		for (int i = 0; i < size; i++) {
	//			String rm = (String) aVector.elementAt(i);
	//			if (rm.equals("Mw")) {
					MwHashTable.commit(id);
	//			} 
	//		}
			map.remove(id);
			return true;
		}
		return false;
	}

	static boolean abort(int id) {
		Vector aVector;
		if (map.containsKey(id)) {
			aVector = map.get(id);
			int size = aVector.size();
	//		for (int i = 0; i < size; i++) {
	//			String rm = (String) aVector.elementAt(i);
	//			if (rm.equals("Mw")) {
					MwHashTable.abort(id);
	//			} 
	//		}
	//	}
		map.remove(id);
		return true;
	}
	return false;
}
}
