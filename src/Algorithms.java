import java.awt.Point;
import java.util.ArrayList;

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

		// go to the lowest point
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

		// makes a loop round the outside of the customer
		for (int mode = 1; mode <= 4; mode++) {
			for (int i = 0; i < Main.customers.size(); i++) {
				int curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
				Point curCustomer = Main.customers.get(curCustomerIndex).location;
				int leftMostIndex = calculateLeftMostPointIndex(curCustomer, mode);
				if (leftMostIndex == -1) {
					break;
				}
				Main.bestPath.add(leftMostIndex);
			}
		}

		for (int n = 0; n < Main.customers.size(); n++) {
			// finds the lowest point which is not connected
			largestY = -1;
			largestYIndex = -1;
			for (int i = 0; i < Main.customers.size(); i++) {
				if (Main.bestPath.contains(i)) {
					continue;
				}
				Point curPoint = Main.customers.get(i).location;
				if (curPoint.y > largestY) {
					largestY = curPoint.y;
					largestYIndex = i;
				}
			}
			if (largestYIndex == -1) {
				break;
			}

			// connects this point to the circle
			double bestTime = 999999999;
			double bestDistance = 999999999;
			int bestInsert = -1;
			for (int i = 1; i < Main.bestPath.size(); i++) {
				// calulates the cost if the point was inserted here
				ArrayList<Integer> path = new ArrayList<Integer>(Main.bestPath);
				path.add(i, largestYIndex);
				double[] timeDistance = calculateTimeDistance(path);
				if (timeDistance[0] < bestTime) {
					bestTime = timeDistance[0];
					bestInsert = i;
				} else if (timeDistance[0] == bestTime && timeDistance[1] < bestDistance) {
					bestDistance = timeDistance[1];
					bestInsert = i;
				}
			}
			Main.bestPath.add(bestInsert, largestYIndex);
		}
	}

	// garinteed perfect answer
	public static void calculateBranchBound(ArrayList<Integer> customersLeft) {
		
		if (customersLeft.size() == 0) {
			//finished searching
			return
		}
		
		for (int i=0; i<customersLeft.size(); i++) {
			
		}
	}

	public static int calculateLeftMostPointIndex(Point start, int mode) {
		int LeftMostPointIndex = -1;
		double LeftMostPointVal = 999999999;
		if (mode == 2) {
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
				if (slope < LeftMostPointVal && curPoint.x < start.x) {
					LeftMostPointVal = slope;
					LeftMostPointIndex = i;
				}
				break;
			case 2:
				if (slope > LeftMostPointVal && curPoint.y < start.y) {
					LeftMostPointVal = slope;
					LeftMostPointIndex = i;
				}
				break;
			case 3:
				if (slope < LeftMostPointVal && curPoint.x > start.x) {
					LeftMostPointVal = slope;
					LeftMostPointIndex = i;
				}
				break;
			case 4:
				if (slope < LeftMostPointVal && curPoint.y > start.y) {
					LeftMostPointVal = slope;
					LeftMostPointIndex = i;
				}
				break;
			}
		}
		return LeftMostPointIndex;
	}

	// returns an array of where element 1 is the time late and element 2 is the
	// total disance of the path
	public static double[] calculateTimeDistance(ArrayList<Integer> path) {
		double[] distances = CalculateTotalDistance(path);

		float lateMins = 0;
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			float time = (float) (distances[i] / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		double[] output = { lateMins, distances[distances.length - 1] };
		return output;
	}

	public static float calculateMinutesOver(ArrayList<Integer> path) {
		double[] distances = CalculateTotalDistance(path);

		float lateMins = 0;
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			float time = (float) (distances[i] / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}
		return lateMins;
	}

	public static double[] CalculateTotalDistance(ArrayList<Integer> path) {
		double[] distances = new double[path.size()];
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers.get(path.get(i - 1)).location;
				end = Main.customers.get(path.get(i)).location;
			}
			double distance = Map.calculateDistance(start, end);
			if (i == 0) {
				distances[i] = distance;
				// distances[i] = 0; TODO Fix
			} else {
				distances[i] = distance + distances[i - 1];
			}
		}
		return distances;
	}
}