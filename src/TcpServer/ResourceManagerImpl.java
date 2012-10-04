
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class ResourceManagerImpl extends Thread implements ResourceManager
{
	protected RMHashtable m_itemHT = new RMHashtable();
	static ObjectInputStream iis;
	static PrintStream os;
	static Socket clientSocket = null;

	public ResourceManagerImpl(Socket client) 
	{
		clientSocket = client;
	}

	public void run()
	{
		try 
		{
			iis = new ObjectInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		while (true) 
		{
			ArrayList<Object> array = new ArrayList<Object>();
			try
			{
				array = (ArrayList<Object>) iis.readObject();
			}
			catch(Exception e)
			{
			}	
			if(array != null)
			{
				try 
				{
					boolean ret = reflector(array, os);
					if (ret)
					{
						System.out.println("Threaded operation Worked!");
					}
					else
					{
						System.out.println("Threaded operation failed");
					}
				} 
				catch (RemoteException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		
	}

	// Reads a data item
	private RMItem readData( int id, String key )
	{
		synchronized(m_itemHT){
			return (RMItem) m_itemHT.get(key);
		}
	}

	// Writes a data item
	private void writeData( int id, String key, RMItem value )
	{
		synchronized(m_itemHT){
			m_itemHT.put(key, value);
		}
	}

	// Remove the item out of storage
	protected RMItem removeData(int id, String key){
		synchronized(m_itemHT){
			return (RMItem)m_itemHT.remove(key);
		}
	}


	// deletes the entire item
	protected boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) {
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} else {
			if(curObj.getReserved()==0){
				removeData(id, curObj.getKey());
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else{
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}


	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key) {
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;  
		if( curObj != null ) {
			value = curObj.getCount();
		} // else
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	

	// query the price of an item
	protected int queryPrice(int id, String key){
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) {
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}

	// reserve an item
	protected boolean reserveItem(int id, int customerID, String key, String location)
			throws RemoteException
			{
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) {
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		} 

		// check if the item is available
		/*ReservableItem item = (ReservableItem)readData(id, key);
			if(item==null){
				Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
				return false;
			}else if(item.getCount()==0){
				Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
				return false;
			}*/
		int price;
		if(key.charAt(0) == 'f')
		{
			price = 1; //rmF.reserveItemHelper(id, customerID, key, location);
		}
		else if(key.charAt(0) == 'c')
		{
			price = 1; //rmC.reserveItemHelper(id, customerID, key, location);
		}
		else
		{
			price = 1; //rmH.reserveItemHelper(id, customerID, key, location);
		}
		if(price == -1)
		{
			return false;
		}
		else
		{			
			cust.reserve( key, location, price);		
			writeData( id, cust.getKey(), cust );

			// decrease the number of available items in the storage
			//item.setCount(item.getCount() - 1);
			//item.setReserved(item.getReserved()+1);

			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return true;
		}		
			}
	public int reserveItemHelper(int id, int customerID, String key, String location)
			throws RemoteException
			{
		return 0;
			}

	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
			throws RemoteException
			{
		Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called on middleware" );
		/*Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
			if( curObj == null ) {
				// doesn't exist...add it
				Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
				writeData( id, newObj.getKey(), newObj );
				Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
						flightSeats + ", price=$" + flightPrice );
			} else {
				// add seats to existing flight and update the price...
				curObj.setCount( curObj.getCount() + flightSeats );
				if( flightPrice > 0 ) {
					curObj.setPrice( flightPrice );
				} // if
				writeData( id, curObj.getKey(), curObj );
				Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
			} // else*/
		return true; //(rmF.addFlight(id, flightNum, flightSeats, flightPrice));
			}



	public boolean deleteFlight(int id, int flightNum)
			throws RemoteException
			{
		return true; //rmF.deleteFlight(id, flightNum);
			}



	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
			throws RemoteException
			{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		/*Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
			if( curObj == null ) {
				// doesn't exist...add it
				Hotel newObj = new Hotel( location, count, price );
				writeData( id, newObj.getKey(), newObj );
				Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
			} else {
				// add count to existing object and update price...
				curObj.setCount( curObj.getCount() + count );
				if( price > 0 ) {
					curObj.setPrice( price );
				} // if
				writeData( id, curObj.getKey(), curObj );
				Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
			} // else*/
		return true; //(rmH.addRooms(id, location, count, price));
			}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
			throws RemoteException
			{
		return true; //rmH.deleteRooms(id, location);

			}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
			throws RemoteException
			{
		Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		/*Car curObj = (Car) readData( id, Car.getKey(location) );
			if( curObj == null ) {
				// car location doesn't exist...add it
				Car newObj = new Car( location, count, price );
				writeData( id, newObj.getKey(), newObj );
				Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
			} else {
				// add count to existing car location and update price...
				curObj.setCount( curObj.getCount() + count );
				if( price > 0 ) {
					curObj.setPrice( price );
				} // if
				writeData( id, curObj.getKey(), curObj );
				Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
			} // else*/
		return true; //(rmC.addCars(id, location, count, price));
			}


	// Delete cars from a location
	public boolean deleteCars(int id, String location)
			throws RemoteException
			{
		return true; //rmC.deleteCars(id, location);
			}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
			throws RemoteException
			{
		return 1; //rmF.queryFlight(id, flightNum);
			}

	// Returns the number of reservations for this flight. 
	//		public int queryFlightReservations(int id, int flightNum)
	//			throws RemoteException
	//		{
	//			Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") called" );
	//			RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
	//			if( numReservations == null ) {
	//				numReservations = new RMInteger(0);
	//			} // if
	//			Trace.info("RM::queryFlightReservations(" + id + ", #" + flightNum + ") returns " + numReservations );
	//			return numReservations.getValue();
	//		}


	// Returns price of this flight
	public int queryFlightPrice(int id, int flightNum )
			throws RemoteException
			{
		return 1; //rmF.queryFlightPrice(id, flightNum);
			}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
			throws RemoteException
			{
		return 1; //rmH.queryRooms(id, location);
			}


	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
			throws RemoteException
			{
		return 1; //rmH.queryRoomsPrice(id, location);
			}


	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
			throws RemoteException
			{
		return 1; //rmC.queryCars(id, location);
			}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
			throws RemoteException
			{
		return 1; //rmC.queryCarsPrice(id, location);
			}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
			throws RemoteException
			{
		Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return null;
		} else {
			return cust.getReservations();
		} // if
			}

	// return a bill
	public String queryCustomerInfo(int id, int customerID)
			throws RemoteException
			{
		Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
		} else {
			String s = cust.printBill();
			Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
			System.out.println( s );
			return s;
		} // if
			}

	// customer functions
	// new customer just returns a unique customer identifier

	public int newCustomer(int id)
			throws RemoteException
			{
		Trace.info("INFO: RM::newCustomer(" + id + ") called" );
		// Generate a globally unique ID for the new customer
		int cid = Integer.parseInt( String.valueOf(id) +
				String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf( Math.round( Math.random() * 100 + 1 )));
		Customer cust = new Customer( cid );
		writeData( id, cust.getKey(), cust );
		Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid );
		return cid;
			}

	// I opted to pass in customerID instead. This makes testing easier
	public boolean newCustomer(int id, int customerID )
			throws RemoteException
			{
		Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			cust = new Customer(customerID);
			writeData( id, cust.getKey(), cust );
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
			return true;
		} else {
			Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
			return false;
		} // else
			}


	// Deletes customer from the database. 
	public boolean deleteCustomer(int id, int customerID)
			throws RemoteException
			{
		Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
		if( cust == null ) {
			Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
			return false;
		} else {			
			// Increase the reserved numbers of all reservable items which the customer reserved. 
			RMHashtable reservationHT = cust.getReservations();
			for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){		
				String reservedkey = (String) (e.nextElement());
				ReservedItem reserveditem = cust.getReservedItem(reservedkey);
				Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
				boolean temp;
				if(reserveditem.getKey().charAt(0) == 'f')
				{
					temp = true; //rmF.removeReservation(id, reserveditem.getKey(), reserveditem.getCount());
				}
				else if(reserveditem.getKey().charAt(0) == 'c')
				{
					temp = true; //rmC.removeReservation(id, reserveditem.getKey(), reserveditem.getCount());
				}
				else
				{
					temp = true; //rmH.removeReservation(id, reserveditem.getKey(), reserveditem.getCount());
				}
				if(!temp)
				{
					return false;
				}
				//ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
				//Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
				//item.setReserved(item.getReserved()-reserveditem.getCount());
				//item.setCount(item.getCount()+reserveditem.getCount());
			}

			// remove the customer from the storage
			removeData(id, cust.getKey());

			Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
			return true;
		}
			}

	public boolean removeReservation(int id, String key, int count)
			throws RemoteException
			{
		return true;
			}


	// Frees flight reservation record. Flight reservation records help us make sure we
	//  don't delete a flight if one or more customers are holding reservations
	//		public boolean freeFlightReservation(int id, int flightNum)
	//			throws RemoteException
	//		{
	//			Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") called" );
	//			RMInteger numReservations = (RMInteger) readData( id, Flight.getNumReservationsKey(flightNum) );
	//			if( numReservations != null ) {
	//				numReservations = new RMInteger( Math.max( 0, numReservations.getValue()-1) );
	//			} // if
	//			writeData(id, Flight.getNumReservationsKey(flightNum), numReservations );
	//			Trace.info("RM::freeFlightReservations(" + id + ", " + flightNum + ") succeeded, this flight now has "
	//					+ numReservations + " reservations" );
	//			return true;
	//		}
	//	

	// Adds car reservation to this customer. 
	public boolean reserveCar(int id, int customerID, String location)
			throws RemoteException
			{
		return reserveItem(id, customerID, Car.getKey(location), location);
			}

	// Adds room reservation to this customer. 
	public boolean reserveRoom(int id, int customerID, String location)
			throws RemoteException
			{
		return reserveItem(id, customerID, Hotel.getKey(location), location);
			}
	// Adds flight reservation to this customer.  
	public boolean reserveFlight(int id, int customerID, int flightNum)
			throws RemoteException
			{
		return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
			}

	/* reserve an itinerary */
	public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean Room)
			throws RemoteException {
		Customer cust = (Customer) readData( id, Customer.getKey(customer) );		
		if( cust == null ) {
			Trace.warn("RM::itinerary( " + id + ", " + customer + ", " + location+")  failed--customer doesn't exist" );
			return false;
		}
		if(car)
		{
			int carPrice = 1; //rmC.reserveItemHelper(id, customer,Car.getKey(location), location);
			if(carPrice == -1)
			{
				return false;
			}
			cust.reserve(Car.getKey(location), location, carPrice);		
			writeData(id, cust.getKey(), cust);
		}
		if(Room)
		{
			int roomPrice = 1; //rmH.reserveItemHelper(id, customer,Hotel.getKey(location), location);
			if(roomPrice == -1)
			{
				return false;
			}
			cust.reserve(Hotel.getKey(location), location, roomPrice);		
			writeData( id, cust.getKey(), cust);
		}
		Iterator iterator = flightNumbers.iterator();
		int flightPrice;
		while(iterator.hasNext())
		{
			int flightNum = Integer.parseInt(iterator.next().toString());
			flightPrice = 1; //rmF.reserveItemHelper(id, customer, Flight.getKey(flightNum), String.valueOf(flightNum));
			if(flightPrice == -1)
			{
				return false;
			}
			cust.reserve(Flight.getKey(flightNum), String.valueOf(flightNum), flightPrice);		
			writeData( id, cust.getKey(), cust );
		}
		Trace.info("RM::Reserve Itinerary(" + id + ", " + customer + ") Succeded");
		return true;
	}
	public boolean reflector(ArrayList<Object> array, PrintStream os) throws RemoteException
	{
		Object[] argument = array.toArray();
		if(((String) argument[0]).compareToIgnoreCase("addFlight")==0)
		{
			boolean ret = addFlight(((Integer) argument[1]).intValue(), ((Integer) argument[2]).intValue(), ((Integer) argument[3]).intValue(), ((Integer) argument[4]).intValue());
			os.print(ret);
			return true;
		}
		/*else if(argument.compareToIgnoreCase("newcar")==0)
			return 3;
		else if(argument.compareToIgnoreCase("newroom")==0)
			return 4;
		else if(argument.compareToIgnoreCase("newcustomer")==0)
			return 5;
		else if(argument.compareToIgnoreCase("deleteflight")==0)
			return 6;
		else if(argument.compareToIgnoreCase("deletecar")==0)
			return 7;
		else if(argument.compareToIgnoreCase("deleteroom")==0)
			return 8;
		else if(argument.compareToIgnoreCase("deletecustomer")==0)
			return 9;
		else if(argument.compareToIgnoreCase("queryflight")==0)
			return 10;
		else if(argument.compareToIgnoreCase("querycar")==0)
			return 11;
		else if(argument.compareToIgnoreCase("queryroom")==0)
			return 12;
		else if(argument.compareToIgnoreCase("querycustomer")==0)
			return 13;
		else if(argument.compareToIgnoreCase("queryflightprice")==0)
			return 14;
		else if(argument.compareToIgnoreCase("querycarprice")==0)
			return 15;
		else if(argument.compareToIgnoreCase("queryroomprice")==0)
			return 16;
		else if(argument.compareToIgnoreCase("reserveflight")==0)
			return 17;
		else if(argument.compareToIgnoreCase("reservecar")==0)
			return 18;
		else if(argument.compareToIgnoreCase("reserveroom")==0)
			return 19;
		else if(argument.compareToIgnoreCase("itinerary")==0)
			return 20;
		else if (argument.compareToIgnoreCase("quit")==0)
			return 21;
		else if (argument.compareToIgnoreCase("newcustomerid")==0)
			return 22;*/
		else
		{
			return false;
		}
	}
}
