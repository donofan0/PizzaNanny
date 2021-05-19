import java.awt.Point;

public class Algorithms {
	// drivers speed it 1000 meters per minute
	static int driverSpeed = 1000;

	public static void calculateNearestNeighbor() {
		Main.bestPath.clear();
		double shortestDistance = 999999999;
		int nextCustomer = 0;
		for (int i = 0; i < Main.customers.size(); i++) {
			Customer customerNextPossibly = Main.customers.get(i);
			double distance = Map.calculateDistance(Map.apachePizza, customerNextPossibly.location);
			if (distance < shortestDistance) {
				shortestDistance = distance;
				nextCustomer = i;
			}
		}
		Main.bestPath.add(nextCustomer);

		for (int i = 0; i < Main.customers.size() - 1; i++) {
			double shortestDist = 999999999;
			int nextCus = -1;
			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
			Customer curCustomer = Main.customers.get(curCustomerIndex);
			for (int j = 0; j < Main.customers.size(); j++) {
				if (curCustomerIndex == j) {
					continue;
				}
				Customer customerNextPossibly = Main.customers.get(j);
				double distance = Map.calculateDistance(curCustomer.location, customerNextPossibly.location);
				if (distance < shortestDist && !Main.bestPath.contains(j)) {
					shortestDist = distance;
					nextCus = j;
				}
			}
			Main.bestPath.add(nextCus);
		}
	}

	public static void calculateConvexHull() {
		Main.bestPath.clear();

		int largestY = -1;
		int largestYIndex = -1;
		for (int i = 0; i < Main.customers.size(); i++) {
			Point curPoint = Main.customers.get(i).location;
			if (curPoint.y > largestY) {
				largestY = curPoint.y;
				largestYIndex = i;
			}
		}
		Main.bestPath.add(largestYIndex);

		for (int i = 0; i < Main.customers.size() - 1; i++) {
			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
			Point curCustomer = Main.customers.get(curCustomerIndex).location;
			int leftMostIndex = calculateLeftMostPointIndex(curCustomer, 1);
			if (leftMostIndex == -1) {
				break;
			}
			Main.bestPath.add(leftMostIndex);
		}
		for (int i = 0; i < Main.customers.size() - 1; i++) {
			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
			Point curCustomer = Main.customers.get(curCustomerIndex).location;
			int leftMostIndex = calculateLeftMostPointIndex(curCustomer, 2);
			if (leftMostIndex == -1) {
				break;
			}
			Main.bestPath.add(leftMostIndex);
		}
//		for (int i = 0; i < Main.customers.size() - 1; i++) {
//			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
//			Point curCustomer = Main.customers.get(curCustomerIndex).location;
//			int leftMostIndex = calculateLeftMostPointIndex(curCustomer, 3);
//			if (leftMostIndex == -1) {
//				break;
//			}
//			Main.bestPath.add(leftMostIndex);
//		}
//		for (int i = 0; i < Main.customers.size() - 1; i++) {
//			int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
//			Point curCustomer = Main.customers.get(curCustomerIndex).location;
//			int leftMostIndex = calculateLeftMostPointIndex(curCustomer, 4);
//			if (Main.bestPath.contains(leftMostIndex)) {
//				break;
//			}
//
//			Main.bestPath.add(leftMostIndex);
//		}
	}

	public static int calculateLeftMostPointIndex(Point start, int mode) {
		int LeftMostPointIndex = 9999999;
		double LeftMostPointVal = 9999999;
		if (mode == 2 || mode == 4) {
			LeftMostPointVal = 0;
		}
		for (int i = 0; i < Main.customers.size(); i++) {
			if (Main.bestPath.contains(i)) {
				continue;
			}
			Point curPoint = Main.customers.get(i).location;
			double slope = Map.calculateSlope(start, curPoint);
			switch (mode) {
			case 1:
				if (slope > LeftMostPointVal && curPoint.x < start.x) {
					LeftMostPointVal = slope;
					LeftMostPointIndex = i;
				}
				break;
			case 2:
				if (Math.abs(slope) > LeftMostPointVal && curPoint.y > start.y) {
					LeftMostPointVal = Math.abs(slope);
					LeftMostPointIndex = i;
				}
				break;
			case 3:
				if (Math.abs(slope) < LeftMostPointVal && curPoint.x > start.x) {
					LeftMostPointVal = Math.abs(slope);
					LeftMostPointIndex = i;
				}
				break;
			case 4:
				if (Math.abs(slope) > LeftMostPointVal && curPoint.y < start.y) {
					LeftMostPointVal = Math.abs(slope);
					LeftMostPointIndex = i;
				}
				break;
			}
		}
		switch (mode) {
		case 1:
			if (LeftMostPointVal < 0) {
				return -1;
			} else {
				return LeftMostPointIndex;
			}
		case 2:
			if (LeftMostPointVal < 0) {
				return -1;
			} else {
				return LeftMostPointIndex;
			}
		case 3:
			if (LeftMostPointVal < 0) {
				return -1;
			} else {
				return LeftMostPointIndex;
			}
		case 4:
			if (LeftMostPointVal < 0) {
				return -1;
			} else {
				return LeftMostPointIndex;
			}
		}
		return -1;
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