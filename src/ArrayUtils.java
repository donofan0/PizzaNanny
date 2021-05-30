public class ArrayUtils {

	public static boolean pathContains(int[] path, int in) {
		for (int i = 0; i < path.length; i++) {
			if (path[i] == in) {
				return true;
			}
		}
		return false;
	}

	public static int[] pathSubList(int[] path, int size) {
		int[] output = new int[size];
		for (int i = 0; i < size; i++) {
			output[i] = path[i];
		}
		return output;
	}

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

	public static int pathFindIndex(int[] path, int id) {
		for (int i = 0; i < path.length; i++) {
			if (path[i] == id) {
				return i;
			}
		}
		return -1;
	}

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
