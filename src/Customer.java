
public class Customer {
	String address;
	int waitTime;
	double latitude;
	double longitude;
	long x;
	long y;

	public Customer() {

	}

	public Customer(String address, int waitTime, double latitude, double longitude) {
		this.address = address;
		this.waitTime = waitTime;
		this.latitude = latitude;
		this.longitude = longitude;
		this.x = Map.latitudeToX(latitude);
		this.y = Map.longitudeToY(longitude);

		if (x > Main.largestDistance) {
			Main.largestDistance = x;
		}

		if (y > Main.largestDistance) {
			Main.largestDistance = y;
		}
	}
}
