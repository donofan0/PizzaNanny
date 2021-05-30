import java.awt.Point;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.SwingWorker;

public class Algorithms {
	public final static String[] algorithms = { "Nearest Neighbor(Distance)", "Nearest Neighbor(Time)", "Convex Hull(Distance)", "Convex Hull(Time)", "Largest Time", "Group Aproximition", "Branch and Bound", "All of the Above" };
	public static boolean algorithemRunning = false;

	// most algorithms have an argument minimizeTime which is a boolean
	// if it is true it means the algorithm is trying to minimize time
	// whereas if its false it will try and minimize distance

	// nearest neighbor algorithm
	// limits:
	// Doesn't consider time
	// Procedure:
	// 1. finds the point closes to Apache pizza and travels to it
	// 2. then from this point, it finds the next closest point which it has not traveled to
	// 3. repeats this until it has a complete path
	// 4. then applies the rework algorithm
	public static void calculateNearestNeighbor(boolean minimizeTime) {
		TSP.bestPath = new int[TSP.customers.length];
		Arrays.fill(TSP.bestPath, -1);

		for (int i = -1; i < TSP.customers.length - 1; i++) {
			int curCustomerIndex;
			Point curCustomer;

			if (i == -1) {
				curCustomerIndex = -1;
				curCustomer = Map.apachePizza;
			} else {
				curCustomerIndex = TSP.bestPath[i];
				curCustomer = TSP.customers[curCustomerIndex].location;
			}

			int nextCus = -1;
			double shortestDist = 999999999;
			for (int j = 0; j < TSP.customers.length; j++) {
				if (curCustomerIndex == j) {
					continue;
				}
				Customer customerNextPossibly = TSP.customers[j];
				double distance = curCustomer.distance(customerNextPossibly.location);
				if (distance < shortestDist && !ArrayUtils.pathContains(TSP.bestPath, j)) {
					shortestDist = distance;
					nextCus = j;
				}
			}
			TSP.bestPath[i + 1] = nextCus;
		}

		ReworkBestPath(minimizeTime);
	}

	// 1.2 Convex Hull algorithm
	// limits:
	// bias to a clockwise path starting at the top point
	// Procedure:
	// 1. travels to the heights point
	// 2. make a loop round the outside points enclosing all other points
	// this works by going left until we reach the end of the graph
	// then down, then right, then up, this means it travels clockwise
	// 3. add any remaining points to the existing path in the location
	// which minimizes the cost(time/distance = boolean minimizeTime)
	// 4. then applies the rework algorithm
	public static void calculateConvexHull(boolean minimizeTime) {
		TSP.bestPath = new int[TSP.customers.length];
		Arrays.fill(TSP.bestPath, -1);

		// go to the highest point
		long lowestY = 999999999;
		int lowestYIndex = -1;
		for (int i = 0; i < TSP.customers.length; i++) {
			Point curPoint = TSP.customers[i].location;
			if (curPoint.y < lowestY) {
				lowestY = curPoint.y;
				lowestYIndex = i;
			}
		}
		TSP.bestPath[0] = lowestYIndex;

		// makes a loop round the outside of the customer
		for (int mode = 1; mode <= 4; mode++) {
			for (int i = 0; i < TSP.customers.length; i++) {
				int curCustomerIndex = TSP.bestPath[i];
				Point curCustomer = TSP.customers[curCustomerIndex].location;
				int leftMostIndex = calculateLeftMostPointIndex(curCustomer, mode);
				if (leftMostIndex == -1) {
					break;
				}
				TSP.bestPath[i + 1] = leftMostIndex;
			}
		}

		// connect the center point
		for (int n = 0; n < TSP.customers.length; n++) {
			// finds the lowest point which is not connected
			lowestY = 999999999;
			lowestYIndex = -1;
			for (int i = 0; i < TSP.customers.length; i++) {
				if (ArrayUtils.pathContains(TSP.bestPath, i)) {
					continue;
				}
				Point curPoint = TSP.customers[i].location;
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
			int[] currentPath = ArrayUtils.trimPath(TSP.bestPath);
			for (int i = 0; i < currentPath.length; i++) {
				// calulates the cost if the point was inserted here
				double[] timeDistance = calculateTimeDistance(ArrayUtils.pathInsert(currentPath, i, lowestYIndex));
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
			TSP.bestPath = ArrayUtils.bestPathInsert(bestInsert, lowestYIndex);
		}

		ReworkBestPath(minimizeTime);

	}

	// Largest Time First Algorithm
	// does not factor in travel time
	// Procedure:
	// 1. finds the customer with the largest starting time and not visited yet
	// 2. go to this customer
	// 3. repeats this until all customers are visited
	// 4. then applies the rework algorithm
	public static void calculateLargestTimeFirst() {
		TSP.bestPath = new int[TSP.customers.length];
		Arrays.fill(TSP.bestPath, -1);

		for (int i = 0; i < TSP.customers.length; i++) {
			int largestTime = -1;
			int largestTimeIndex = -1;
			for (int j = 0; j < TSP.customers.length; j++) {
				Customer customer = TSP.customers[j];
				if (customer.waitTime > largestTime && !ArrayUtils.pathContains(TSP.bestPath, j)) {
					largestTime = customer.waitTime;
					largestTimeIndex = j;
				}
			}
			TSP.bestPath[i] = largestTimeIndex;
		}
		ReworkBestPath(true);
	}

	// Group Approximation
	// i designed it from the ground up and it runs asynchronously on a new thread
	// updates the progress bar
	// limits:
	// Doesn't work well past 60 points
	// Procedure:
	// 1. find the closest groups which contain groupSize customers
	// 2. run branch and bound algorithm on each group
	// 3. then travel from one group to the next
	// if number of customers <= 60
	// it decides the order of the groups by trying all possibles
	// then it chooses the one with the lowest time
	// if number of customers > 60
	// it decides the order of the groups by going from the group
	// with the highest time to the group with the next highest and so on.
	// 4. then applies the rework algorithm
	public static void calculateGroupApproximation(final int[][] algoritemsPaths) {
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
				if (TSP.customers.length > 60) {
					groupSize = 10;
				} else if (TSP.customers.length > 50) {
					groupSize = 10;
				} else if (TSP.customers.length >= 6) {
					groupSize = 6;
				}
				int numOfGroups = (int) Math.ceil((float) TSP.customers.length / (float) groupSize);
				maxIterations = Algorithms.calculateFactorial(numOfGroups);

				TSP.bestPath = new int[TSP.customers.length];
				for (int i = 0; i < TSP.customers.length; i++) {
					TSP.bestPath[i] = i;
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
					Point curCustomer = TSP.customers[curCustomerIndex].location;

					int[] group = new int[groupSize];
					Arrays.fill(group, -1);
					for (int a = 0; a < groupSize; a++) {
						int closestCustomer = -1;
						double shortestDist = 999999999;
						for (int j = 0; j < TSP.customers.length; j++) {
							if (closestCustomer == j) {
								continue;
							}
							Customer closestCustomerPossibly = TSP.customers[j];
							double distance = curCustomer.distance(closestCustomerPossibly.location);
							if (distance < shortestDist && !ArrayUtils.pathContains(group, j) && !groupsContains(groups, j)) {
								shortestDist = distance;
								closestCustomer = j;
							}
						}
						group[a] = closestCustomer;
					}
					groups[i] = Arrays.copyOf(calculateBranchAndBound(ArrayUtils.trimPath(group)), ArrayUtils.trimPath(group).length);
				}

				double bestTime = 999999999;
				int localBestGroup = -1;
				int[] localBestPath = new int[TSP.customers.length];
				int[] permitation = new int[numOfGroups];
				Arrays.fill(permitation, -1);
				if (TSP.customers.length > 60) {
					for (int i = 0; i < numOfGroups; i++) {
						bestTime = -1;
						for (int groupIndex = 0; groupIndex < numOfGroups; groupIndex++) {
							int[] group = groups[groupIndex];
							float time = (float) calculateTime(group);
							if (time > bestTime && !ArrayUtils.pathContains(permitation, groupIndex)) {
								bestTime = time;
								localBestGroup = groupIndex;
							}
						}
						permitation[i] = localBestGroup;
					}

					int[] path = new int[TSP.customers.length];
					for (int groupIndex = 0; groupIndex < numOfGroups; groupIndex++) {
						int group = permitation[groupIndex];
						for (int point = 0; point < groups[group].length; point++) {
							if (groupIndex * groupSize + point > TSP.customers.length - 1) {
								break;
							}
							path[groupIndex * groupSize + point] = groups[group][point];
						}
					}

					TSP.bestPath = path;

					ReworkBestPath(true);
					return true;
				}

				for (int i = 0; i < permitation.length; i++) {
					permitation[i] = i;
				}
				int[] path = new int[TSP.customers.length];
				for (int groupIndex = 0; groupIndex < permitation.length; groupIndex++) {
					int group = permitation[groupIndex];
					for (int point = 0; point < groups[group].length; point++) {
						if (groupIndex * groupSize + point > TSP.customers.length - 1) {
							break;
						}
						path[groupIndex * groupSize + point] = groups[group][point];
					}
				}
				double time = calculateTime(path);
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

						path = new int[TSP.customers.length];
						int currentPathIndex = 0;
						for (int groupIndex = 0; groupIndex < permitation.length; groupIndex++) {
							int group = permitation[groupIndex];
							for (int point = 0; point < groups[group].length; point++) {
								path[currentPathIndex] = groups[group][point];
								currentPathIndex++;
							}
						}
						time = calculateTime(path);
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

				TSP.bestPath = localBestPath;

				ReworkBestPath(true);
				return true;
			}

			@Override
			protected void process(List<Long> chunks) {
				/*
				 * sets the label to its new coordinates and size this is called in batches as in it could execute
				 * like this doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get the next item in the
				 * list
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

						double[] timeDistance = calculateTimeDistance();
						String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
						String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

						data[6] = convertToTable(algorithms[algorithms.length - 3], 28);
						data[6] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
						data[6] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
						data[6] += convertToTable(hour + ":" + min, 21);
						data[6] += convertToTable(timeFromat.format(currentTime - startTime), 28);
						data[6] += "|";

						Gui.algCompare.setListData(data);
					}
				}

				if (algoritemsPaths != null) {
					double bestTime = 999999999;
					int bestAlg = -1;
					for (int i = 0; i < algoritemsPaths.length; i++) {
						double time = calculateTime(algoritemsPaths[i]);
						if (time < bestTime) {
							bestTime = time;
							bestAlg = i;
						}
					}
					if (calculateTime() > bestTime) {
						TSP.bestPath = algoritemsPaths[bestAlg];
					} else {
						ControlPanel.bestAlgorithem.setText("Best: " + algorithms[algorithms.length - 3]);
					}
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

	// Branch and Bound
	// Generates perfect answer
	// limits:
	// Really slow which makes it impractical for more than 13 customers
	// it runs asynchronously on a new thread
	// updates the progress bar
	// Procedure:
	// 1. generate next possible path.
	// 2. calculate its cost(time/distance = boolean minimizeTime)
	// if while calculating cost it is already higher than the lowest cost
	// it will discount that route and not calculate it anymore
	// 3. chose the path with the lowest cost
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

				maxIterations = Algorithms.calculateFactorial(TSP.customers.length);

				TSP.bestPath = new int[TSP.customers.length];

				int[] path = new int[TSP.customers.length];
				for (int i = 0; i < TSP.customers.length; i++) {
					path[i] = i;
					TSP.bestPath[i] = i;
				}
				bestTime = calculateTimeUpTo(path, 999999999, true);

				int[] swapWith = new int[TSP.customers.length];
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
								TSP.bestPath[j] = path[j];
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
				 * sets the label to its new coordinates and size this is called in batches as in it could execute
				 * like this doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get the next item in the
				 * list
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

						double[] timeDistance = calculateTimeDistance();
						String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
						String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

						data[7] = convertToTable(algorithms[algorithms.length - 2], 28);
						data[7] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
						data[7] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
						data[7] += convertToTable(hour + ":" + min, 21);
						data[7] += convertToTable(timeFromat.format(currentTime - startTime), 28);
						data[7] += "|";

						Gui.algCompare.setListData(data);
					}
				}
			}
		};

		// executes the swingworker on worker thread
		findPaths.execute();
	}

	// Branch and Bound
	// Generates perfect answer
	// limits:
	// Really slow which makes it impractical for more than 13 customers#
	// the synchronous version of the above algorithm
	// without progress bar
	// Procedure:
	// same as above
	// 1. generate next possible path.
	// 2. calculate its cost(time/distance = boolean minimizeTime)
	// if while calculating cost it is already higher than the lowest cost
	// it will discount that route and not calculate it anymore
	// 3. chose the path with the lowest cost
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

	// improves the answer to any of the algorithms
	// limits:
	// only considers moving one point at a time
	// for example in the path 1,2,3,4,5 it may improve time if you moved both 3 and 4
	// but if you just moved 3 it might increase the cost until you move 4 as well
	// Procedure:
	// 1. calculate the cost(time/distance = boolean minimizeTime)
	// of inserting the customer in each of the possible different locations
	// 2. insert the customer in the location with the minimum cost
	// 3. repeat this for all the customers
	private static void ReworkBestPath(boolean minimizeTime) {
		for (int repeat = 0; repeat < ControlPanel.repeatSteps; repeat++) {
			for (int n = 0; n < TSP.customers.length; n++) {
				// connects this point to the circle
				ArrayUtils.bestPathRemove(n);
				double bestTime = 999999999;
				double bestDistance = 999999999;
				int bestInsert = -1;
				for (int i = 0; i < TSP.bestPath.length + 1; i++) {
					// Calculates the cost if the point was inserted here
					double[] timeDistance = calculateTimeDistance(ArrayUtils.pathInsert(TSP.bestPath, i, n));
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
				TSP.bestPath = ArrayUtils.pathInsert(TSP.bestPath, bestInsert, n);
			}
		}
	}

	// runs all the algorithms and compiles the results into an array of strings
	// where each element in the array is a algorithm.
	// this array is formated is such a way that if displayed
	// with a mono font it displays as a table.
	// It then returns this array so that it can be displayed in the comparison
	// screen
	public static String[] compareAlogrithemsWithResults() {
		String[] output = new String[algorithms.length - 2];
		// header for the table
		output[0] = "|          Algorithm         | Distance | Angry Mins | Journey Time(hh:mm) | Proccessing Time(mm:ss:ms) |";

		// goes through each synchronous algorithm
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

			double[] timeDistance = calculateTimeDistance();
			String hour = numFormat.format((int) ((timeDistance[1] / Map.driverSpeed) / 60));
			String min = numFormat.format((timeDistance[1] / Map.driverSpeed) % 60);

			output[i + 1] = convertToTable(algorithms[i], 28);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[1]) + "m", 10);
			output[i + 1] += convertToTable(numFormat.format(timeDistance[0]) + " min", 12);
			output[i + 1] += convertToTable(hour + ":" + min, 21);
			output[i + 1] += convertToTable(timeFromat.format(currentTime - startTime), 28);
			output[i + 1] += "|";
		}

		// starts the two asynchronous algorithms
		calculateGroupApproximation(null);

		if (TSP.customers.length < 12) {
			calculateBranchAndBound();
		}

		return output;
	}

	// runs and compares the algorithms and returns an array of with
	// each row is the path generated by an algorithm
	// sets the bestPath to the best of these algorithms
	public static int compareAlogrithems() {
		int bestAlgorithem = 0;
		double bestTime = 999999999;
		int[][] algoritemsPaths = new int[algorithms.length - 3][TSP.customers.length];
		// goes through each synchronous algorithm
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
				calculateGroupApproximation(null);
				break;
			}

			double time = calculateTime();
			if (time < bestTime) {
				bestAlgorithem = i;
				bestTime = time;
			}

			algoritemsPaths[i] = TSP.bestPath;
		}

		TSP.bestPath = algoritemsPaths[bestAlgorithem];

		// starts the two asynchronous algorithms
		calculateGroupApproximation(algoritemsPaths);
		if (TSP.customers.length < 12) {
			calculateBranchAndBound();
		}

		return bestAlgorithem;
	}

	/*
	 * Algorithms Utilities
	 */

	// returns true if groups contains the value
	// otherwise it returns false
	private static boolean groupsContains(int[][] groups, int value) {
		for (int i = 0; i < groups.length; i++) {
			if (ArrayUtils.pathContains(groups[i], value)) {
				return true;
			}
		}
		return false;
	}

	// calculates the time taken for the provided path and
	// stops when it reaches the max(maximum lateMins)
	// if include Apache is true then it will assume that it starts the path at Apache
	// otherwise it assumes it starts at the first customer in the path
	private static float calculateTimeUpTo(int[] path, float max, boolean includeApache) {
		float lastDistance = 0;
		float lateMins = 0;
		Point start;
		Customer endCustomer;

		if (includeApache) {
			start = Map.apachePizza;
			endCustomer = TSP.customers[path[0]];

			lastDistance = (float) start.distance(endCustomer.location);

			float time = (lastDistance / Map.driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		for (int i = 1; i < path.length; i++) {
			start = TSP.customers[path[i - 1]].location;
			endCustomer = TSP.customers[path[i]];

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

	// returns the factorial of the number provided
	// returns in!
	public static double calculateFactorial(int in) {
		if (in == 1 || in == 2) {
			return in;
		} else if (in == 3) {
			return 6;
		}

		return in * calculateFactorial(in - 1);
	}

	// finds the point to the mode(direction) of the current point
	// mode = 1 = left, mode = 2 = down, mode = 3 = right, mode 4 = up
	public static int calculateLeftMostPointIndex(Point start, int mode) {
		int LeftMostPointIndex = -1;
		double LeftMostPointVal = 999999999;
		if (mode == 2) {
			LeftMostPointVal = 0;
		}
		for (int i = 0; i < TSP.customers.length; i++) {
			if (ArrayUtils.pathContains(TSP.bestPath, i)) {
				continue;
			}
			Point curPoint = TSP.customers[i].location;
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

	// returns the provided string with a pipe at the start
	// and padded so that it is colmbWidth in length
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

	// returns the time that curCustomer has waited
	// where curCustomer is the customer at pathIndex in the bestPath
	public static float calculateTimeWaiting(Customer curCustomer, int pathIndex) {
		if (TSP.bestPath.length < 1 || pathIndex < 0) {
			return -1;
		}

		double[] distances = CalculateTotalDistance(ArrayUtils.pathSubList(TSP.bestPath, pathIndex + 1));
		float time = (float) (distances[distances.length - 1] / Map.driverSpeed) + curCustomer.waitTime;
		return time;
	}

	// returns the angry minutes taken for the provided path
	public static double calculateTime(int[] path) {
		return calculateTimeDistance(path)[0];
	}

	// returns the angry minutes taken for the best path
	public static double calculateTime() {
		return calculateTimeDistance(TSP.bestPath)[0];
	}

	// returns an array of where element 1 is the angry minutes taken for the provided path
	// and element 2 is the total distance of the path
	public static double[] calculateTimeDistance(int[] path) {
		double[] distances = CalculateTotalDistance(path);

		double lateMins = 0;
		for (int i = 0; i < path.length; i++) {
			Customer endCustomer = TSP.customers[path[i]];
			double time = (distances[i] / Map.driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		double[] output = { lateMins, distances[distances.length - 1] };
		return output;
	}

	// returns an array of where element 1 is the angry minutes taken for the bestPath
	// and element 2 is the total distance of the bestPath
	public static double[] calculateTimeDistance() {
		return calculateTimeDistance(TSP.bestPath);
	}

	// returns the total distance taken to follow the provided path
	public static double[] CalculateTotalDistance(int[] path) {
		double[] distances = new double[path.length];
		for (int i = 0; i < path.length; i++) {
			Customer endCustomer = TSP.customers[path[i]];
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = TSP.customers[path[i - 1]].location;
				end = TSP.customers[path[i]].location;
			}
			double distance = start.distance(end);
			if (i == 0) {
				distances[i] = distance;
			} else {
				distances[i] = distance + distances[i - 1];
			}
		}
		return distances;
	}
}