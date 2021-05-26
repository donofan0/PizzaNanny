import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

public class Algorithms {

	public static int compareAlogrithems() {
		int bestAlgorithem = 0;
		double bestTime = 999999999;
		for (int i = 0; i < 5; i++) {
			switch (i) {
			case 0:
				calculateNearestNeighbor(false);
				break;
			case 1:
				calculateNearestNeighbor(true);
				break;
			case 2:
				calculateConvexHull(false);
				break;
			case 3:
				calculateConvexHull(true);
				break;
			case 4:
				calculateLargestTimeFirst();
				break;
			}
			double time = Map.calculateTime();
			if (time < bestTime) {
				bestAlgorithem = i;
				bestTime = time;
			}
		}

		switch (bestAlgorithem) {
		case 0:
			calculateNearestNeighbor(false);
			break;
		case 1:
			calculateNearestNeighbor(true);
			break;
		case 2:
			calculateConvexHull(false);
			break;
		case 3:
			calculateConvexHull(true);
			break;
		case 4:
			calculateLargestTimeFirst();
			break;
		}
		return bestAlgorithem;
	}

	public static void calculateNearestNeighbor(boolean minimizeTime) {
		Main.bestPath.clear();

		for (int i = -1; i < Main.customers.size() - 1; i++) {
			int curCustomerIndex;
			Point curCustomer;
			if (i == -1) {
				curCustomerIndex = -1;
				curCustomer = Map.apachePizza;
			} else {
				curCustomerIndex = Main.bestPath.get(Main.bestPath.size() - 1);
				curCustomer = Main.customers.get(curCustomerIndex).location;
			}

			int nextCus = -1;
			double shortestDist = 999999999;
			for (int j = 0; j < Main.customers.size(); j++) {
				if (curCustomerIndex == j) {
					continue;
				}
				Customer customerNextPossibly = Main.customers.get(j);
				double distance = curCustomer.distance(customerNextPossibly.location);
				if (distance < shortestDist && !Main.bestPath.contains(j)) {
					shortestDist = distance;
					nextCus = j;
				}
			}
			Main.bestPath.add(nextCus);
		}

		ReworkBestPath(minimizeTime);
	}

	public static void calculateConvexHull(boolean minimizeTime) {
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

		// connect the center point
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
			for (int i = 0; i < Main.bestPath.size(); i++) {
				// calulates the cost if the point was inserted here
				ArrayList<Integer> path = new ArrayList<Integer>(Main.bestPath);
				path.add(i, largestYIndex);
				double[] timeDistance = Map.calculateTimeDistance(path);
				if (timeDistance[0] < bestTime && minimizeTime) {
					bestTime = timeDistance[0];
					bestInsert = i;
				} else if (timeDistance[1] < bestDistance && !minimizeTime) {
					bestDistance = timeDistance[1];
					bestInsert = i;
				} else if (timeDistance[1] < bestDistance && timeDistance[0] == bestTime) {
					bestDistance = timeDistance[1];
					bestInsert = i;
				}
			}
			Main.bestPath.add(bestInsert, largestYIndex);
		}

		ReworkBestPath(minimizeTime);

	}

	public static void calculateLargestTimeFirst() {
		Main.bestPath.clear();

		for (int i = 0; i < Main.customers.size(); i++) {
			int largestTime = -1;
			int largestTimeIndex = -1;
			for (int j = 0; j < Main.customers.size(); j++) {
				Customer customer = Main.customers.get(j);
				if (customer.waitTime > largestTime && !Main.bestPath.contains(j)) {
					largestTime = customer.waitTime;
					largestTimeIndex = j;
				}
			}
			Main.bestPath.add(largestTimeIndex);
		}
		ReworkBestPath(true);
	}

	// Guaranteed perfect answer
	public static void calculateBranchBound(ArrayList<Integer> customersLeft) {

		if (customersLeft.size() == 0) {
			// finished searching
			// return
		}

		for (int i = 0; i < customersLeft.size(); i++) {

		}
	}

	private static void ReworkBestPath(boolean minimizeTime) {
		for (int repeat = 0; repeat < ControlPanel.repeatSteps; repeat++) {
			for (int n = 0; n < Main.customers.size(); n++) {
				// connects this point to the circle
				Main.bestPath.removeAll(Collections.singleton(n));
				double bestTime = 999999999;
				double bestDistance = 999999999;
				int bestInsert = -1;
				for (int i = 0; i < Main.bestPath.size() + 1; i++) {
					// calulates the cost if the point was inserted here
					ArrayList<Integer> path = new ArrayList<Integer>(Main.bestPath);
					path.add(i, n);
					double[] timeDistance = Map.calculateTimeDistance(path);
					if (timeDistance[0] < bestTime && minimizeTime) {
						bestTime = timeDistance[0];
						bestInsert = i;
					} else if (timeDistance[1] < bestDistance && !minimizeTime) {
						bestDistance = timeDistance[1];
						bestInsert = i;
					} else if (timeDistance[1] < bestDistance && timeDistance[0] == bestTime) {
						bestDistance = timeDistance[1];
						bestInsert = i;
					}
				}
				Main.bestPath.add(bestInsert, n);
			}
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
}