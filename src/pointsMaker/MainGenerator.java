package pointsMaker;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Scanner;

public class MainGenerator {
	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.4105);
	final static Point2D.Double bottomRigthMap = new Point2D.Double(-6.4546, 53.2857);
	final static double earthRadius = 6371.0070072;

	public static void main(String[] args) {

		try (Scanner scan = new Scanner(System.in)) {
			Random rand = new Random();

			int maxPoints = 100;
			int numOfClusters = 5;
			double deviationX = 0.001;
			double deviationY = 0.001;
			for (int i = 0; i < numOfClusters; i++) {
				double meanX = (bottomRigthMap.x - topLeftMap.x) * rand.nextDouble() + topLeftMap.x;
				double meanY = (bottomRigthMap.y - topLeftMap.y) * rand.nextDouble() + topLeftMap.y;
				for (int j = 0; j < maxPoints / numOfClusters; j++) {
					double x = (rand.nextGaussian() * deviationX + meanX);
					double y = (rand.nextGaussian() * deviationY + meanY);
					int time = rand.nextInt(31);
					System.out.println((j + i * (maxPoints / numOfClusters) + 1) + ",This is a test address ," + time
							+ "," + y + "," + x);
				}
			}
		}
	}

	public static double calcGPSDistance(double lat1, double long1, double lat2, double long2) {
		// convert to degrees
		lat1 *= Math.PI / 180;
		long1 *= Math.PI / 180;
		lat2 *= Math.PI / 180;
		long2 *= Math.PI / 180;

		double deltaLamda = absDiffRad(long1, long2);
		// gets the distance exactly as a float the shifts it 2 places to the right
		// then rounds it to the nearest long, then shifts it 2 places to the left
		// meaning it rounds it to the nearest 100.
		double deltaSigma = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLamda));
		return earthRadius * deltaSigma * 1000;
	}

	public static int latitudeToY(double latitude) {
		return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
	}

	public static int longitudeToX(double longitude) {
		return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
	}

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}
}
