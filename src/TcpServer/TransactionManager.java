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
		aVector = map.get(id);
		if(!aVector.contains(rm))
		{
			aVector.add(rm);
		}
		map.put(id, aVector);
	}

	static Vector commit(int id) {
		Vector aVector;
		aVector = map.get(id);
		MwHashTable.commit(id);
		map.remove(id);
		return aVector;
	}

	static Vector abort(int id) {
		Vector aVector;
		aVector = map.get(id);
		MwHashTable.abort(id);
		map.remove(id);
		return aVector;
}
}
