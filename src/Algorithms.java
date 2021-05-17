import java.awt.Point;

public class Algorithms {
	// drivers speed it 1000 meters per minute
	static int driverSpeed = 1000;

	public static void calculateNearestNeighbor() {
		Main.bestPath.clear();
		double shortestDistance = 999999999;
		int nextCustomer = 0;
		for (int x = 0; x < Main.customers.size(); x++) {
			Customer customerNextPossibly = Main.customers.get(x);
			double distance = Map.calculateDistance(Map.apachePizza, customerNextPossibly.location);
			if (distance < shortestDistance) {
				shortestDistance = distance;
				nextCustomer = x;
			}
		}
		Main.bestPath.add(nextCustomer);

		for (int i = 0; i < Main.customers.size() - 1; i++) {
			double shortestDist = 999999999;
			int nextCus = -1;
			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
			Customer curCustomer = Main.customers.get(curCustomerIndex);
			for (int x = 0; x < Main.customers.size(); x++) {
				if (curCustomerIndex == x) {
					continue;
				}
				Customer customerNextPossibly = Main.customers.get(x);
				double distance = Map.calculateDistance(curCustomer.location, customerNextPossibly.location);
				if (distance < shortestDist && !Main.bestPath.contains(x)) {
					shortestDist = distance;
					nextCus = x;
				}
			}
			Main.bestPath.add(nextCus);
		}
	}

	public static float calculateMinutesOver() {
		double[] distances = new double[Main.bestPath.size()];
		for (int i = 0; i < Main.bestPath.size(); i++) {
			Customer endCustomer = Main.customers.get(Main.bestPath.get(i));
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers.get(Main.bestPath.get(i - 1)).location;
				end = Main.customers.get(Main.bestPath.get(i)).location;
			}
			double distance = Map.calculateDistance(start, end);
			if (i == 0) {
				distances[i] = distance;
			} else {
				distances[i] = distance + distances[i - 1];
			}
		}

		float lateMins = 0;
		for (int i = 0; i < Main.bestPath.size(); i++) {
			Customer endCustomer = Main.customers.get(Main.bestPath.get(i));
			float time = (float) (distances[i] / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}
		return lateMins;
	}
}