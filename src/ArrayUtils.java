public class ArrayUtils {

	// returns whether the specified path contains in
	public static boolean pathContains(int[] path, int in) {
		for (int i = 0; i < path.length; i++) {
			if (path[i] == in) {
				return true;
			}
		}
		return false;
	}

	// returns a subPath of the path provided from index 0 to index size-1
	public static int[] pathSubList(int[] path, int size) {
		int[] output = new int[size];
		for (int i = 0; i < size; i++) {
			output[i] = path[i];
		}
		return output;
	}

	// returns the best path with the value inserted and index
	// returned array is always the size of customers
	public static int[] bestPathInsert(int index, int value) {
		int[] newBestPath = new int[TSP.customers.length];
		for (int i = 0; i < newBestPath.length; i++) {
			if (i < index) {
				newBestPath[i] = TSP.bestPath[i];
			} else if (i == index) {
				newBestPath[i] = value;
			} else {
				newBestPath[i] = TSP.bestPath[i - 1];
			}
		}
		return newBestPath;
	}

	// returns the provided path with the value inserted and index
	// returned array is always the size of path+1
	public static int[] pathInsert(int[] path, int index, int value) {
		int[] newBestPath = new int[path.length + 1];
		for (int i = 0; i < newBestPath.length; i++) {
			if (i < index) {
				newBestPath[i] = path[i];
			} else if (i == index) {
				newBestPath[i] = value;
			} else {
				newBestPath[i] = path[i - 1];
			}
		}
		return newBestPath;
	}

	// finds the the occorance of id in the path provided
	// returns the index where id was found
	// or -1 if it was not found
	public static int pathFindIndex(int[] path, int id) {
		for (int i = 0; i < path.length; i++) {
			if (path[i] == id) {
				return i;
			}
		}
		return -1;
	}

	// searches and removes the specified value from the bestPath
	public static void bestPathRemove(int value) {
		int[] newBestPath = new int[TSP.bestPath.length - 1];
		boolean found = false;
		for (int i = 0; i < newBestPath.length; i++) {
			if (!found && TSP.bestPath[i] == value) {
				found = true;
			}

			if (found) {
				newBestPath[i] = TSP.bestPath[i + 1];
			} else {
				newBestPath[i] = TSP.bestPath[i];
			}
		}
		TSP.bestPath = newBestPath;
	}

	// returns the path provided with all -1 removed from it
	public static int[] trimPath(int[] path) {
		int size = path.length;
		for (int i = 0; i < path.length; i++) {
			if (path[i] == -1) {
				size = i;
				break;
			}
		}

		int[] output = new int[size];
		for (int i = 0; i < size; i++) {
			output[i] = path[i];
		}
		return output;
	}
}
