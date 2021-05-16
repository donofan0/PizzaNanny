import java.awt.Point;

public class Customer {
	String address;
	int waitTime;
	double latitude;
	double longitude;
	Point location;

	public Customer() {

	}

	public Customer(String address, int waitTime, double latitude, double longitude) {
		this.address = address;
		this.waitTime = waitTime;
		this.latitude = latitude;
		this.longitude = longitude;
		location = Points.latLongtoPoint(latitude, longitude);
	}
}
