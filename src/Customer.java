import java.awt.Point;

public class Customer implements Comparable<Customer> {
	Integer id;
	String address;
	int waitTime;
	double latitude;
	double longitude;
	Point location;

	public Customer() {

	}

	public Customer(int id, String address, int waitTime, double latitude, double longitude) {
		this.id = id;
		this.address = address;
		this.waitTime = waitTime;
		this.latitude = latitude;
		this.longitude = longitude;
		location = Map.latLongtoPoint(latitude, longitude);
	}

	@Override
	public int compareTo(Customer customer) {
		return this.id.compareTo(customer.id);
	}
}
