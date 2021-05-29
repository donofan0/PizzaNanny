import java.awt.Point;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.SwingWorker;

public class Algorithms {
	public final static String[] algorithms = { "Nearest Neighbor(Distance)", "Nearest Neighbor(Time)",
			"Convex Hull(Distance)", "Convex Hull(Time)", "Largest Time", "Group Aproximition", "Branch and Bound",
			"All of the Above" };
	public static boolean algorithemRunning = false;

	public static String[] compareAlogrithemsWithResults() {
		String[] output = new String[algorithms.length - 2];
		output[0] = "|          Algorithm         | Distance | Angry Mins | Journey Time(hh:mm) | Proccessing Time(mm:ss:ms) |";
		for (int i = 0; i < algorithms.length - 3; i++) {
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
			}

			long currentTime = System.currentTimeMillis();
			NumberFormat numFormat = NumberFormat.getInstance();
			numFormat.setMaximumFractionDigits(0);
			numFormat.setMinimumIntegerDigits(2);
			SimpleDateFormat timeFromat = new SimpleDateFormat("mm:ss:SSS");

			double[] timeDistance = Map.calculateTimeDistance();
			String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
			String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

			output[i + 1] = convertToTable(algorithms[i], 28);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
			output[i + 1] += convertToTable(hour + ":" + min, 21);
			output[i + 1] += convertToTable(timeFromat.format(currentTime - startTime), 28);
			output[i + 1] += "|";
		}

		calculateGroupAproximition(null);

		if (Main.customers.length < 12) {
			calculateBranchAndBound();
		}

		return output;
	}

	public static int compareAlogrithems() {
		int bestAlgorithem = 0;
		double bestTime = 999999999;
		int[][] algoritemsPaths = new int[algorithms.length - 3][Main.customers.length];
		for (int i = 0; i < algorithms.length - 3; i++) {
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
				calculateGroupAproximition(null);
				break;
			}

			double time = Map.calculateTime();
			if (time < bestTime) {
				bestAlgorithem = i;
				bestTime = time;
			}

			algoritemsPaths[i] = Main.bestPath;
		}

		Main.bestPath = algoritemsPaths[bestAlgorithem];

		calculateGroupAproximition(algoritemsPaths);
		if (Main.customers.length < 12) {
			calculateBranchAndBound();
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

		// go to the heighest point
		long lowestY = 999999999;
		int lowestYIndex = -1;
		for (int i = 0; i < Main.customers.length; i++) {
			Point curPoint = Main.customers[i].location;
			if (curPoint.y < lowestY) {
				lowestY = curPoint.y;
				lowestYIndex = i;
			}
		}
		Main.bestPath[0] = lowestYIndex;

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
			lowestY = 999999999;
			lowestYIndex = -1;
			for (int i = 0; i < Main.customers.length; i++) {
				if (Map.bestPathContains(i)) {
					continue;
				}
				Point curPoint = Main.customers[i].location;
				if (curPoint.y < lowestY) {
					lowestY = curPoint.y;
					lowestYIndex = i;
				}
			}
			if (lowestYIndex == -1) {
				break;
			}

			// connects this point to the circle
			double bestTime = 999999999;
			double bestDistance = 999999999;
			int bestInsert = -1;
			int[] currentPath = Map.trimPath(Main.bestPath);
			for (int i = 0; i < currentPath.length; i++) {
				// calulates the cost if the point was inserted here
				double[] timeDistance = Map.calculateTimeDistance(Map.pathInsert(currentPath, i, lowestYIndex));
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
			if (bestInsert == -1) {
				// error occurred fallback to nearest neighbor
				calculateNearestNeighbor(minimizeTime);
				System.out.println("!!Convex Algorithem Error, Calling Nearest Neighbor fallback");
				return;
			}
			Main.bestPath = Map.bestPathInsert(bestInsert, lowestYIndex);
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

	public static void calculateGroupAproximition(final int[][] algoritemsPaths) {
		SwingWorker<Boolean, Long> findPaths = new SwingWorker<Boolean, Long>() {
			private long startTime = System.currentTimeMillis();
			private double maxIterations = -1;

			@Override
			protected Boolean doInBackground() throws Exception {
				while (ControlPanel.branchAlgorithmRunning) {
					Thread.sleep(1);
				}
				ControlPanel.groupAlgorithmRunning = true;

				int groupSize = 1;
				if (Main.customers.length > 60) {
					groupSize = 10;
				} else if (Main.customers.length > 50) {
					groupSize = 10;
				} else if (Main.customers.length >= 6) {
					groupSize = 6;
				}
				int numOfGroups = (int) Math.ceil((float) Main.customers.length / (float) groupSize);
				maxIterations = Algorithms.calculateFactorial(numOfGroups);

				Main.bestPath = new int[Main.customers.length];
				for (int i = 0; i < Main.customers.length; i++) {
					Main.bestPath[i] = i;
				}

				int[][] groups = new int[numOfGroups][groupSize];

				for (int i = 0; i < numOfGroups; i++) {
					Arrays.fill(groups[i], -1);
				}

				for (int i = 0; i < numOfGroups; i++) {
					int curCustomerIndex = 0;
					while (groupsContains(groups, curCustomerIndex)) {
						curCustomerIndex++;
					}
					Point curCustomer = Main.customers[curCustomerIndex].location;

					int[] group = new int[groupSize];
					Arrays.fill(group, -1);
					for (int a = 0; a < groupSize; a++) {
						int closestCustomer = -1;
						double shortestDist = 999999999;
						for (int j = 0; j < Main.customers.length; j++) {
							if (closestCustomer == j) {
								continue;
							}
							Customer closestCustomerPossibly = Main.customers[j];
							double distance = curCustomer.distance(closestCustomerPossibly.location);
							if (distance < shortestDist && !Map.pathContains(group, j) && !groupsContains(groups, j)) {
								shortestDist = distance;
								closestCustomer = j;
							}
						}
						group[a] = closestCustomer;
					}
					groups[i] = Arrays.copyOf(calculateBranchAndBound(Map.trimPath(group)), Map.trimPath(group).length);
				}

				double bestTime = 999999999;
				int localBestGroup = -1;
				int[] localBestPath = new int[Main.customers.length];
				int[] permitation = new int[numOfGroups];
				Arrays.fill(permitation, -1);
				if (Main.customers.length > 60) {
					for (int i = 0; i < numOfGroups; i++) {
						bestTime = -1;
						for (int groupIndex = 0; groupIndex < numOfGroups; groupIndex++) {
							int[] group = groups[groupIndex];
							float time = (float) Map.calculateTime(group);
							if (time > bestTime && !Map.pathContains(permitation, groupIndex)) {
								bestTime = time;
								localBestGroup = groupIndex;
							}
						}
						permitation[i] = localBestGroup;
					}

					int[] path = new int[Main.customers.length];
					for (int groupIndex = 0; groupIndex < numOfGroups; groupIndex++) {
						int group = permitation[groupIndex];
						for (int point = 0; point < groups[group].length; point++) {
							if (groupIndex * groupSize + point > Main.customers.length - 1) {
								break;
							}
							path[groupIndex * groupSize + point] = groups[group][point];
						}
					}

					Main.bestPath = path;

					ReworkBestPath(true);
					return true;
				}

				for (int i = 0; i < permitation.length; i++) {
					permitation[i] = i;
				}
				int[] path = new int[Main.customers.length];
				for (int groupIndex = 0; groupIndex < permitation.length; groupIndex++) {
					int group = permitation[groupIndex];
					for (int point = 0; point < groups[group].length; point++) {
						if (groupIndex * groupSize + point > Main.customers.length - 1) {
							break;
						}
						path[groupIndex * groupSize + point] = groups[group][point];
					}
				}
				double time = Map.calculateTime(path);
				if (time < bestTime) {
					bestTime = time;
					localBestPath = Arrays.copyOf(path, path.length);
				}

				int[] swapWith = new int[permitation.length];
				int i = 0;
				long count = 0;
				while (i < swapWith.length && ControlPanel.groupAlgorithmRunning) {
					if (swapWith[i] < i) {
						if (i % 2 == 0) {
							int temp = permitation[i];
							permitation[i] = permitation[0];
							permitation[0] = temp;
						} else {
							int temp = permitation[i];
							permitation[i] = permitation[swapWith[i]];
							permitation[swapWith[i]] = temp;
						}

						swapWith[i]++;
						i = 0;
						count++;

						path = new int[Main.customers.length];
						int currentPathIndex = 0;
						for (int groupIndex = 0; groupIndex < permitation.length; groupIndex++) {
							int group = permitation[groupIndex];
							for (int point = 0; point < groups[group].length; point++) {
								path[currentPathIndex] = groups[group][point];
								currentPathIndex++;
							}
						}
						time = Map.calculateTime(path);
						if (time < bestTime) {
							bestTime = time;
							localBestPath = Arrays.copyOf(path, path.length);
						}
						publish(count);
					} else {
						swapWith[i] = 0;
						i++;
					}
				}

				Main.bestPath = localBestPath;

				ReworkBestPath(true);
				return true;
			}

			@Override
			protected void process(List<Long> chunks) {
				/*
				 * sets the label to its new coordinates and size this is called in batches as
				 * in it could execute like this
				 * doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get
				 * the next item in the list
				 */
				long count = chunks.get(chunks.size() - 1);

				ControlPanel.progress.setValue((int) Math.round((count * 100) / maxIterations));
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution
				if (Gui.algCompare != null) {
					ListModel model = Gui.algCompare.getModel();

					if (model.getSize() < algorithms.length) {
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
						String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
						String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

						data[data.length - 1] = convertToTable(algorithms[algorithms.length - 3], 28);
						data[data.length - 1] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
						data[data.length - 1] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
						data[data.length - 1] += convertToTable(hour + ":" + min, 21);
						data[data.length - 1] += convertToTable(timeFromat.format(currentTime - startTime), 28);
						data[data.length - 1] += "|";

						Gui.algCompare.setListData(data);
					}
					ControlPanel.bestAlgorithem.setText("Best: " + algorithms[algorithms.length - 3]);
				}

				if (algoritemsPaths != null) {
					double bestTime = 999999999;
					int bestAlg = -1;
					for (int i = 0; i < algoritemsPaths.length; i++) {
						double time = Map.calculateTime(algoritemsPaths[i]);
						if (time < bestTime) {
							bestTime = time;
							bestAlg = i;
						}
					}
					if (Map.calculateTime() > bestTime) {
						Main.bestPath = algoritemsPaths[bestAlg];
					} else {
						ControlPanel.bestAlgorithem.setText("Best: " + algorithms[algorithms.length - 3]);
					}
				} else {
					ControlPanel.bestAlgorithem.setText("Best: " + algorithms[algorithms.length - 3]);
				}

				Gui.map.drawLines();
				Gui.ctrlPanel.drawOutput();
				ControlPanel.progress.setValue(100);
				ControlPanel.groupAlgorithmRunning = false;
			}
		};

		// executes the swingworker on worker thread
		findPaths.execute();
	}

	// Guaranteed perfect answer
	public static void calculateBranchAndBound() {
		SwingWorker<Boolean, Long> findPaths = new SwingWorker<Boolean, Long>() {
			private float bestTime = 999999999;
			private long startTime = System.currentTimeMillis();
			private double maxIterations = -1;

			@Override
			protected Boolean doInBackground() throws Exception {
				while (ControlPanel.groupAlgorithmRunning) {
					Thread.sleep(1);
				}
				ControlPanel.branchAlgorithmRunning = true;

				maxIterations = Algorithms.calculateFactorial(Main.customers.length);

				Main.bestPath = new int[Main.customers.length];

				int[] path = new int[Main.customers.length];
				for (int i = 0; i < Main.customers.length; i++) {
					path[i] = i;
					Main.bestPath[i] = i;
				}
				bestTime = calculateTimeUpTo(path, 999999999, true);

				int[] swapWith = new int[Main.customers.length];
				int i = 0;
				long count = 0;
				while (i < swapWith.length && ControlPanel.branchAlgorithmRunning) {
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

						float time = calculateTimeUpTo(path, bestTime, true);
						if (time < bestTime) {
							for (int j = 0; j < path.length; j++) {
								Main.bestPath[j] = path[j];
							}
							bestTime = time;
						}

						count++;
						swapWith[i]++;
						i = 0;

						publish(count);
					} else {
						swapWith[i] = 0;
						i++;
					}
				}
				return true;
			}

			@Override
			protected void process(List<Long> chunks) {
				/*
				 * sets the label to its new coordinates and size this is called in batches as
				 * in it could execute like this
				 * doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get
				 * the next item in the list
				 */
				long count = chunks.get(chunks.size() - 1);

				ControlPanel.progress.setValue((int) Math.round((count * 100) / maxIterations));
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution
				Gui.map.drawLines();
				Gui.ctrlPanel.drawOutput();
				ControlPanel.progress.setValue(100);
				ControlPanel.branchAlgorithmRunning = false;

				if (Gui.algCompare != null) {
					ListModel model = Gui.algCompare.getModel();

					if (model.getSize() < algorithms.length) {
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
						String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
						String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

						data[data.length - 1] = convertToTable(algorithms[algorithms.length - 2], 28);
						data[data.length - 1] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
						data[data.length - 1] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
						data[data.length - 1] += convertToTable(hour + ":" + min, 21);
						data[data.length - 1] += convertToTable(timeFromat.format(currentTime - startTime), 28);
						data[data.length - 1] += "|";

						Gui.algCompare.setListData(data);
					}
				}
			}
		};

		// executes the swingworker on worker thread
		findPaths.execute();
	}

	private static int[] calculateBranchAndBound(int[] path) {
		float bestTime = calculateTimeUpTo(path, 999999999, true);
		int[] curBestPath = new int[path.length];
		for (int j = 0; j < curBestPath.length; j++) {
			curBestPath[j] = path[j];
		}

		int[] swapWith = new int[path.length];
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

				float time = calculateTimeUpTo(path, bestTime, false);
				if (time < bestTime) {
					for (int j = 0; j < curBestPath.length; j++) {
						curBestPath[j] = path[j];
					}
					bestTime = time;
				}

				swapWith[i]++;
				i = 0;
			} else {
				swapWith[i] = 0;
				i++;
			}
		}
		return curBestPath;
	}

	private static boolean groupsContains(int[][] groups, int value) {
		for (int i = 0; i < groups.length; i++) {
			if (Map.pathContains(groups[i], value)) {
				return true;
			}
		}
		return false;
	}

	private static float calculateTimeUpTo(int[] path, float max, boolean includeApache) {
		float lastDistance = 0;
		float lateMins = 0;
		Point start;
		Customer endCustomer;

		if (includeApache) {
			start = Map.apachePizza;
			endCustomer = Main.customers[path[0]];

			lastDistance = (float) start.distance(endCustomer.location);

			float time = (lastDistance / Map.driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		for (int i = 1; i < path.length; i++) {
			start = Main.customers[path[i - 1]].location;
			endCustomer = Main.customers[path[i]];

			lastDistance += (float) start.distance(endCustomer.location);

			float time = (lastDistance / Map.driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}

			if (lateMins >= max) {
				return lateMins;
			}
		}
		return lateMins;
	}

	public static double calculateFactorial(int in) {
		if (in == 1 || in == 2) {
			return in;
		} else if (in == 3) {
			return 6;
		}

		return in * calculateFactorial(in - 1);
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
				if (slope < LeftMostPointVal && curPoint.y > start.y) {
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

				if (slope > LeftMostPointVal && curPoint.y < start.y) {
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