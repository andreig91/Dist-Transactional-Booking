
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;



public class ResourceManagerImpl extends Thread implements ResourceManager
{
	//protected RMHashtable m_itemHT = new RMHashtable();
	ObjectInputStream iis;
	DataOutputStream os;
	Socket clientSocket;
	String rmF;
	int rmFPort;
	String rmC;
	int rmCPort;
	String rmH;
	int rmHPort;
	Socket fSocket;
	DataInputStream fIs;
	ObjectOutputStream fOs;
	Socket cSocket;
	DataInputStream cIs;
	ObjectOutputStream cOs;
	Socket hSocket;
	DataInputStream hIs;
	ObjectOutputStream hOs;
	ArrayList<Object> array;
	ArrayList<Object> outArray;

	public ResourceManagerImpl(Socket client,String rmF,int rmFPort,String rmC,int rmCPort,String rmH,int rmHPort) 
	{
		clientSocket = client;
		this.rmF = rmF;
		this.rmFPort = rmFPort;
		this.rmC = rmC;
		this.rmCPort = rmCPort;
		this.rmH = rmH;
		this.rmHPort = rmHPort;
	}
	public void connectToRm()
	{
		try 
		{
			fSocket = new Socket(rmF, rmFPort);
			fOs = new ObjectOutputStream(fSocket.getOutputStream());
			fIs = new DataInputStream(fSocket.getInputStream());
			cSocket = new Socket(rmC, rmCPort);
			cOs = new ObjectOutputStream(cSocket.getOutputStream());
			cIs = new DataInputStream(cSocket.getInputStream());
			hSocket = new Socket(rmH, rmHPort);
			hOs = new ObjectOutputStream(hSocket.getOutputStream());
			hIs = new DataInputStream(hSocket.getInputStream());			
		} 
		catch (UnknownHostException e) 
		{
			System.err.println("Don't know about host");
		} 
		catch (IOException e) 
		{
			System.err.println("Couldn't get I/O for the connection to host");
		}
		if (fSocket != null && fOs != null && fIs != null && cSocket != null && cOs != null && cIs != null
			&& hSocket != null && hOs != null && hIs != null) 
		{
		}
		else
		{
			System.out.println("failed at rmSocket != null && os != null && is != null");
			System.exit(0);
		}
	}

	public void run()
	{
		connectToRm();
		try 
		{
			iis = new ObjectInputStream(clientSocket.getInputStream());
			os = new DataOutputStream(clientSocket.getOutputStream());
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
			if(array!=null)
			{
				if(((String) array.get(0)).compareToIgnoreCase("quit") == 0)
				{
					System.out.println("Client disconnected");
					break;
				}
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
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		
	}

	// Reads a data item
	private RMItem readData( int id, String key )
	{
		return MwHashTable.readData(id, key);			
	}

	// Writes a data item
	private void writeData( int id, String key, RMItem value )
	{
		MwHashTable.writeData(id, key, value);
	}

	// Remove the item out of storage
	protected RMItem removeData(int id, String key)
	{
		return MwHashTable.removeData(id, key);
	}


	// deletes the entire item
	protected boolean deleteItem(int id, String key)
	{
		Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key );
		// Check if there is such an item in the storage
		if( curObj == null ) 
		{
			Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
			return false;
		} 
		else 
		{
			if(curObj.getReserved()==0)
			{
				removeData(id, curObj.getKey());
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
				return false;
			}
		} // if
	}


	// query the number of available seats/rooms/cars
	protected int queryNum(int id, String key)
	{
		Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0;  
		if( curObj != null ) 
		{
			value = curObj.getCount();
		} // else
		Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
		return value;
	}	

	// query the price of an item
	protected int queryPrice(int id, String key)
	{
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
		ReservableItem curObj = (ReservableItem) readData( id, key);
		int value = 0; 
		if( curObj != null ) 
		{
			value = curObj.getPrice();
		} // else
		Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
		return value;		
	}

	// reserve an item
	protected boolean reserveItem(int id, int customerID, String key, String location)
	throws IOException
	{
		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );		
		// Read customer object if it exists (and read lock it)
		Customer cust = (Customer) readData( id, Customer.getKey(customerID) );		
		if( cust == null ) 
		{
			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
			return false;
		}
		int price;
		array = new ArrayList<Object>();
		String method = "reserveItemHelper";
		array.add(method);
		array.add(id);
		array.add(customerID);
		array.add(key);
		array.add(location);
		if(key.charAt(0) == 'f')
		{
			fOs.writeObject(array);
			price = fIs.readInt();
		}
		else if(key.charAt(0) == 'c')
		{
			cOs.writeObject(array);
			price = cIs.readInt();
		}
		else
		{
			hOs.writeObject(array);
			price = hIs.readInt();
		}
		if(price == -1)
		{
			return false;
		}
		else
		{			
			cust.reserve( key, location, price);		
			writeData( id, cust.getKey(), cust );
			Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
			return true;
		}		
	}
	public int reserveItemHelper(int id, int customerID, String key, String location)
	throws IOException
	{
		return 0;
	}

	// Create a new flight, or add seats to existing flight
	//  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
	throws IOException
	{
		Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called on middleware" );
		array = new ArrayList<Object>();
		String method = "addFlight";
		array.add(method);
		array.add(id);
		array.add(flightNum);
		array.add(flightSeats);
		array.add(flightPrice);
		fOs.writeObject(array);
		return fIs.readBoolean();
	}

	public boolean deleteFlight(int id, int flightNum)
	throws IOException
	{
		array = new ArrayList<Object>();
		String method = "deleteFlight";
		array.add(method);
		array.add(id);
		array.add(flightNum);
		fOs.writeObject(array);
		return fIs.readBoolean();
	}

	// Create a new room location or add rooms to an existing location
	//  NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int id, String location, int count, int price)
	throws IOException
	{
		Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
		array = new ArrayList<Object>();
		String method = "addRoom";
		array.add(method);
		array.add(id);
		array.add(location);
		array.add(count);
		array.add(price);
		hOs.writeObject(array);
		return hIs.readBoolean();
	}

	// Delete rooms from a location
	public boolean deleteRooms(int id, String location)
	throws IOException
	{
		return true; //rmH.deleteRooms(id, location);
	}

	// Create a new car location or add cars to an existing location
	//  NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int id, String location, int count, int price)
			throws IOException
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
		array = new ArrayList<Object>();
		String method = "addCar";
		array.add(method);
		array.add(id);
		array.add(location);
		array.add(count);
		array.add(price);
		cOs.writeObject(array);
		return cIs.readBoolean();
			}


	// Delete cars from a location
	public boolean deleteCars(int id, String location)
			throws IOException
			{
		return true; //rmC.deleteCars(id, location);
			}



	// Returns the number of empty seats on this flight
	public int queryFlight(int id, int flightNum)
			throws IOException
			{
		return 1; //rmF.queryFlight(id, flightNum);
			}

	// Returns the number of reservations for this flight. 
	//		public int queryFlightReservations(int id, int flightNum)
	//			throws IOException
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
			throws IOException
			{
		return 1; //rmF.queryFlightPrice(id, flightNum);
			}


	// Returns the number of rooms available at a location
	public int queryRooms(int id, String location)
			throws IOException
			{
		return 1; //rmH.queryRooms(id, location);
			}


	// Returns room price at this location
	public int queryRoomsPrice(int id, String location)
			throws IOException
			{
		return 1; //rmH.queryRoomsPrice(id, location);
			}


	// Returns the number of cars available at a location
	public int queryCars(int id, String location)
			throws IOException
			{
		return 1; //rmC.queryCars(id, location);
			}


	// Returns price of cars at this location
	public int queryCarsPrice(int id, String location)
			throws IOException
			{
		return 1; //rmC.queryCarsPrice(id, location);
			}

	// Returns data structure containing customer reservation info. Returns null if the
	//  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
	//  reservations.
	public RMHashtable getCustomerReservations(int id, int customerID)
			throws IOException
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
			throws IOException
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
			throws IOException
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
			throws IOException
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
			throws IOException
			{
		return true;
			}


	// Frees flight reservation record. Flight reservation records help us make sure we
	//  don't delete a flight if one or more customers are holding reservations
	//		public boolean freeFlightReservation(int id, int flightNum)
	//			throws IOException
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
			throws IOException
			{
		return reserveItem(id, customerID, Car.getKey(location), location);
			}

	// Adds room reservation to this customer. 
	public boolean reserveRoom(int id, int customerID, String location)
			throws IOException
			{
		return reserveItem(id, customerID, Hotel.getKey(location), location);
			}
	// Adds flight reservation to this customer.  
	public boolean reserveFlight(int id, int customerID, int flightNum)
			throws IOException
			{
		return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
			}

	/* reserve an itinerary */
	public boolean itinerary(int id,int customer,Vector flightNumbers,String location,boolean car,boolean Room)
			throws IOException {
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
	public boolean reflector(ArrayList<Object> array, DataOutputStream os) throws IOException
	{
		Object[] argument = array.toArray();
		if(((String) argument[0]).equals("addFlight"))
		{
			boolean ret = addFlight(((Integer) argument[1]).intValue(), ((Integer) argument[2]).intValue(), ((Integer) argument[3]).intValue(), ((Integer) argument[4]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("addCar"))
		{
			boolean ret = addCars(((Integer) argument[1]).intValue(), ((String) argument[2]).toString(), ((Integer) argument[3]).intValue(), ((Integer) argument[4]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("addRoom"))
		{
			boolean ret = addRooms(((Integer) argument[1]).intValue(), ((String) argument[2]).toString(), ((Integer) argument[3]).intValue(), ((Integer) argument[4]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("newCustomer"))
		{
			int ret = newCustomer(((Integer) argument[1]).intValue());
			os.writeInt(ret);
			return true;
		}
		else if (((String) argument[0]).equals("newCustomerId"))
		{
			boolean ret = newCustomer(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("deleteFlight"))
		{
			boolean ret = deleteFlight(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("deleteCar"))
		{
			boolean ret = deleteCars(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("deleteRoom"))
		{
			boolean ret = deleteRooms(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("deleteCustomer"))
		{
			boolean ret = deleteCustomer(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryFlight"))
		{
			int ret = queryFlight(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryCar"))
		{
			int ret = queryCars(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryRoom"))
		{
			int ret = queryRooms(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryCustomer"))
		{
			String ret = queryCustomerInfo(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeBytes(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryFlightPrice"))
		{
			int ret = queryFlightPrice(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryCarPrice"))
		{
			int ret = queryCarsPrice(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("queryRoomPrice"))
		{
			int ret = queryRoomsPrice(((Integer) argument[1]).intValue(),((String) argument[2]).toString());
			os.writeInt(ret);
			return true;
		}
		else if(((String) argument[0]).equals("reserveFlight"))
		{
			boolean ret = reserveFlight(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue(),((Integer) argument[3]).intValue());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("reserveCar"))
		{
			boolean ret = reserveCar(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue(),((String) argument[3]).toString());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("reserveRoom"))
		{
			boolean ret = reserveCar(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue(),((String) argument[3]).toString());
			os.writeBoolean(ret);
			return true;
		}
		else if(((String) argument[0]).equals("itinerary"))
		{
			boolean ret = itinerary(((Integer) argument[1]).intValue(),((Integer) argument[2]).intValue(),((Vector<Integer>) argument[3]),((String) argument[4]).toString(), ((Boolean) argument[5]).booleanValue(),((Boolean) argument[6]).booleanValue());
			os.writeBoolean(ret);
			return true;
		}		
		else
		{
			System.out.println("Method desired was not found");
			return false;
		}
	}
}
