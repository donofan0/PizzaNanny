import java.awt.Point;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.ListModel;
import javax.swing.SwingWorker;

public class Algorithms {
	public final static String[] algorithms = { "Nearest Neighbor(Distance)", "Nearest Neighbor(Time)",
			"Convex Hull(Distance)", "Convex Hull(Time)", "Largest Time", "Depth First Search", "All of the Above" };
	public static boolean algorithemRunning = false;

	public static String[] compareAlogrithemsWithResults() {
		String[] output = new String[algorithms.length];
		output[0] = "|          Algorithm         | Distance | Angry Mins | Journey Time(hh:mm) | Proccessing Time(mm:ss:ms) |";
		for (int i = 0; i < algorithms.length - 1; i++) {
			long startTime = System.currentTimeMillis();

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
			case 5:
				calculateDepthFirstSearch();
				break;
			}

			long currentTime = System.currentTimeMillis();
			NumberFormat numFormat = NumberFormat.getInstance();
			numFormat.setMaximumFractionDigits(0);
			numFormat.setMinimumIntegerDigits(2);
			SimpleDateFormat timeFromat = new SimpleDateFormat("mm:ss:SSS");

			double[] timeDistance = Map.calculateTimeDistance();
			String hour = numFormat.format((timeDistance[1] / Map.driverSpeed) / 60);
			String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

			output[i + 1] = convertToTable(algorithms[i], 28);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
			output[i + 1] += convertToTable(hour + ":" + min, 21);
			output[i + 1] += convertToTable(timeFromat.format(currentTime - startTime), 28);
			output[i + 1] += "|";
		}
		calculateDepthFirstSearchAsync();
		return output;
	}

	public static int compareAlogrithems() {
		int bestAlgorithem = 0;
		double bestTime = 999999999;
		for (int i = 0; i < algorithms.length - 1; i++) {
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
			case 5:
				calculateDepthFirstSearch();
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
		case 5:
			calculateDepthFirstSearch();
			break;
		}
		return bestAlgorithem;
	}

	public static void calculateNearestNeighbor(boolean minimizeTime) {
		Main.bestPath = new int[Main.customers.length];
		Arrays.fill(Main.bestPath, -1);

		for (int i = -1; i < Main.customers.length - 1; i++) {
			int curCustomerIndex;
			Point curCustomer;
			if (i == -1) {
				curCustomerIndex = -1;
				curCustomer = Map.apachePizza;
			} else {
				curCustomerIndex = Main.bestPath[i];
				curCustomer = Main.customers[curCustomerIndex].location;
			}

			int nextCus = -1;
			double shortestDist = 999999999;
			for (int j = 0; j < Main.customers.length; j++) {
				if (curCustomerIndex == j) {
					continue;
				}
				Customer customerNextPossibly = Main.customers[j];
				double distance = curCustomer.distance(customerNextPossibly.location);
				if (distance < shortestDist && !Map.bestPathContains(j)) {
					shortestDist = distance;
					nextCus = j;
				}
			}
			Main.bestPath[i + 1] = nextCus;
		}

		ReworkBestPath(minimizeTime);
	}

	public static void calculateConvexHull(boolean minimizeTime) {
		Main.bestPath = new int[Main.customers.length];
		Arrays.fill(Main.bestPath, -1);

		// go to the lowest point
		long largestY = -1;
		int largestYIndex = -1;
		for (int i = 0; i < Main.customers.length; i++) {
			Point curPoint = Main.customers[i].location;
			if (curPoint.y > largestY) {
				largestY = curPoint.y;
				largestYIndex = i;
			}
		}
		Main.bestPath[0] = largestYIndex;

		// makes a loop round the outside of the customer
		for (int mode = 1; mode <= 4; mode++) {
			for (int i = 0; i < Main.customers.length; i++) {
				int curCustomerIndex = Main.bestPath[i];
				Point curCustomer = Main.customers[curCustomerIndex].location;
				int leftMostIndex = calculateLeftMostPointIndex(curCustomer, mode);
				if (leftMostIndex == -1) {
					break;
				}
				Main.bestPath[i + 1] = leftMostIndex;
			}
		}

		// connect the center point
		for (int n = 0; n < Main.customers.length; n++) {
			// finds the lowest point which is not connected
			largestY = -1;
			largestYIndex = -1;
			for (int i = 0; i < Main.customers.length; i++) {
				if (Map.bestPathContains(i)) {
					continue;
				}
				Point curPoint = Main.customers[i].location;
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
			int[] currentPath = Map.trimPath(Main.bestPath);
			for (int i = 0; i < currentPath.length; i++) {
				// calulates the cost if the point was inserted here
				double[] timeDistance = Map.calculateTimeDistance(Map.pathInsert(currentPath, i, largestYIndex));
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
			Main.bestPath = Map.bestPathInsert(bestInsert, largestYIndex);
		}

		ReworkBestPath(minimizeTime);

	}

	public static void calculateLargestTimeFirst() {
		Main.bestPath = new int[Main.customers.length];
		Arrays.fill(Main.bestPath, -1);

		for (int i = 0; i < Main.customers.length; i++) {
			int largestTime = -1;
			int largestTimeIndex = -1;
			for (int j = 0; j < Main.customers.length; j++) {
				Customer customer = Main.customers[j];
				if (customer.waitTime > largestTime && !Map.bestPathContains(j)) {
					largestTime = customer.waitTime;
					largestTimeIndex = j;
				}
			}
			Main.bestPath[i] = largestTimeIndex;
		}
		ReworkBestPath(true);
	}

	// Guaranteed perfect answer
	public static void calculateDepthFirstSearch() {
		Main.bestPath = new int[Main.customers.length];

		int[] path = new int[Main.customers.length];
		for (int i = 0; i < Main.customers.length; i++) {
			path[i] = i;
			Main.bestPath[i] = i;
		}
		float bestTime = calculateTimeUpTo(path, 999999999);

//		int[] swapWith = new int[Main.customers.length];
//		int i = 0;
//		// long count = 0;
//		while (i < swapWith.length) {
//			if (swapWith[i] < i) {
//				if (i % 2 == 0) {
//					int temp = path[i];
//					path[i] = path[0];
//					path[0] = temp;
//				} else {
//					int temp = path[i];
//					path[i] = path[swapWith[i]];
//					path[swapWith[i]] = temp;
//				}
//
//				float time = calculateTimeUpTo(path, bestTime);
//				if (time < bestTime) {
//					for (int j = 0; j < path.length; j++) {
//						Main.bestPath[j] = path[j];
//					}
//					bestTime = time;
//				}
//				// count++;
//				swapWith[i]++;
//				i = 0;
//			} else {
//				swapWith[i] = 0;
//				i++;
//			}
//		}
		// System.out.println(count);
	}

	private static void calculateDepthFirstSearchAsync() {
		ConcurrentLinkedQueue<int[]> pathQueue = new ConcurrentLinkedQueue<int[]>();
		Algorithms.algorithemRunning = true;

		SwingWorker<Boolean, int[]> findTimes = new SwingWorker<Boolean, int[]>() {
			private float bestTime = 999999999;
			private long startTime = System.currentTimeMillis();

			@Override
			protected Boolean doInBackground() throws Exception {
				while (Algorithms.algorithemRunning || !pathQueue.isEmpty()) {
					while (pathQueue.isEmpty()) {
					}

					int[] path = pathQueue.poll();
					float time = calculateTimeUpTo(path, bestTime);
					if (time < bestTime) {
						for (int j = 0; j < path.length; j++) {
							Main.bestPath[j] = path[j];
						}
						bestTime = time;
					}
				}

				return true;
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution
				ListModel model = Gui.algCompare.getModel();

				String[] data = new String[model.getSize() + 1];
				for (int i = 0; i < model.getSize(); i++) {
					data[i] = (String) model.getElementAt(i);
				}

				long currentTime = System.currentTimeMillis();
				NumberFormat numFormat = NumberFormat.getInstance();
				numFormat.setMaximumFractionDigits(0);
				numFormat.setMinimumIntegerDigits(2);
				SimpleDateFormat timeFromat = new SimpleDateFormat("mm:ss:SSS");

				double[] timeDistance = Map.calculateTimeDistance();
				String hour = numFormat.format((timeDistance[1] / Map.driverSpeed) / 60);
				String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

				data[model.getSize()] = convertToTable("Async Brute Force", 28);
				data[model.getSize()] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
				data[model.getSize()] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
				data[model.getSize()] += convertToTable(hour + ":" + min, 21);
				data[model.getSize()] += convertToTable(timeFromat.format(currentTime - startTime), 28);
				data[model.getSize()] += "|";

				Gui.algCompare.setListData(data);
			}
		};

		SwingWorker<Boolean, int[]> findPaths = new SwingWorker<Boolean, int[]>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				Main.bestPath = new int[Main.customers.length];

				int[] path = new int[Main.customers.length];
				for (int i = 0; i < Main.customers.length; i++) {
					path[i] = i;
					Main.bestPath[i] = i;
				}
				pathQueue.add(path);

				int[] swapWith = new int[Main.customers.length];
				int i = 0;
				while (i < swapWith.length) {
					if (swapWith[i] < i) {
						if (i % 2 == 0) {
							int temp = path[i];
							path[i] = path[0];
							path[0] = temp;
						} else {
							int temp = path[i];
							path[i] = path[swapWith[i]];
							path[swapWith[i]] = temp;
						}

						swapWith[i]++;
						i = 0;

						while (pathQueue.size() > 100000) {
						}
						pathQueue.add(path.clone());
					} else {
						swapWith[i] = 0;
						i++;
					}
				}
				return true;
			}

			@Override
			protected void done() {
				Algorithms.algorithemRunning = false;
			}
		};

		// executes the swingworker on worker thread
		findPaths.execute();
		findTimes.execute();
	}

	private static float calculateTimeUpTo(int[] path, float max) {
		float lastDistance = 0;
		float lateMins = 0;

		Point start = Map.apachePizza;
		Customer endCustomer = Main.customers[path[0]];

		float distance = (float) start.distance(endCustomer.location);
		distance += lastDistance;
		lastDistance = distance;

		float time = (distance / Map.driverSpeed) + endCustomer.waitTime;
		if (time > 30) {
			lateMins += time - 30;
		}

		for (int i = 1; i < path.length; i++) {
			start = Main.customers[path[i - 1]].location;
			endCustomer = Main.customers[path[i]];

			distance = (float) start.distance(endCustomer.location);
			distance += lastDistance;
			lastDistance = distance;

			time = (distance / Map.driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}

			if (lateMins >= max) {
				return lateMins;
			}
		}
		return lateMins;
	}

	private static void ReworkBestPath(boolean minimizeTime) {
		for (int repeat = 0; repeat < ControlPanel.repeatSteps; repeat++) {
			for (int n = 0; n < Main.customers.length; n++) {
				// connects this point to the circle
				Map.bestPathRemove(n);
				double bestTime = 999999999;
				double bestDistance = 999999999;
				int bestInsert = -1;
				for (int i = 0; i < Main.bestPath.length + 1; i++) {
					// calulates the cost if the point was inserted here
					double[] timeDistance = Map.calculateTimeDistance(Map.bestPathInsert(i, n));
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
				Main.bestPath = Map.bestPathInsert(bestInsert, n);
			}
		}
	}

	public static int calculateLeftMostPointIndex(Point start, int mode) {
		int LeftMostPointIndex = -1;
		double LeftMostPointVal = 999999999;
		if (mode == 2) {
			LeftMostPointVal = 0;
		}
		for (int i = 0; i < Main.customers.length; i++) {
			if (Map.bestPathContains(i)) {
				continue;
			}
			Point curPoint = Main.customers[i].location;
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

	private static String convertToTable(String input, int colmbWidth) {
		int spaceNum = colmbWidth - input.length();
		boolean even = true;
		if (spaceNum % 2 == 0) {
			spaceNum /= 2;
		} else {
			spaceNum -= 1;
			spaceNum /= 2;
			even = false;
		}

		String output = "|";

		if (!even) {
			output += " ";
		}

		for (int i = 0; i < spaceNum; i++) {
			output += " ";
		}
		output += input;
		for (int i = 0; i < spaceNum; i++) {
			output += " ";
		}

		return output;
	}
}