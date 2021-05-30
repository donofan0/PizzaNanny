import java.awt.Point;

public class Customer implements Comparable<Customer> {
	Integer id; // the provided number of the customer
	String address;
	int waitTime;
	double latitude;
	double longitude;

	// latiude and longitude which has been converted to my co-ordantates system
	// where 1 on the x or y equals 1 meter in the real world
	Point location;

	// not used
	public Customer() {

	}

	public Customer(int id, String address, int waitTime, double latitude, double longitude) {
		this.id = id;
		this.address = address;

		this.waitTime = waitTime;
		if (this.waitTime < 0) {
			this.waitTime = 0; // corrects incorrectly typed values
		}

		this.latitude = latitude;
		this.longitude = longitude;
		location = Map.latLongtoPoint(latitude, longitude);
	}

	// used to be used to sort the array but is not required anymore
	@Override
	public int compareTo(Customer customer) {
		return this.id.compareTo(customer.id);
	}
}
