package pointsMaker;

import java.awt.geom.Point2D;

public class MainGenerator {
	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.4105);
	// final static Point bottomRigthMap = new Point(longitudeToX(-6.4546),
	// latitudeToY(53.2857));
	final static double earthRadius = 6371.0070072;

	public static void main(String[] args) {

		System.out.println(yToLatitude(latitudeToY(53.2857)));

//		try (Scanner scan = new Scanner(System.in)) {
//			Random rand = new Random();
//
//			int maxPoints = 1;
//			int numOfClusters = 5;
//			double deviationX = 1;
//			double deviationY = 1;
//			for (int i = 0; i < numOfClusters; i++) {
//				double meanX = bottomRigthMap.x * rand.nextDouble();
//				double meanY = bottomRigthMap.y * rand.nextDouble();
//				for (int j = 0; j < maxPoints / numOfClusters; j++) {
//					double x = (rand.nextGaussian() * deviationX + meanX) % bottomRigthMap.x;
//					double y = (rand.nextGaussian() * deviationY + meanY) % bottomRigthMap.y;
//					int time = rand.nextInt(31);
//					System.out.println((j + i * (maxPoints / numOfClusters) + 1) + ",This is a test address ," + time
//							+ "," + y + "," + x);
//				}
//			}
//		}
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

	public static double latitudeToY(double latitude) {
		return calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x);
	}

	public static int longitudeToX(double longitude) {
		return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
	}

	public static double yToLatitude(double y) {
		int n = 9;

		double lat1 = topLeftMap.y;
		double long1 = topLeftMap.x;
		double long2 = topLeftMap.x;
		double deltaLamda = absDiffRad(long1, long2);

		// convert to degrees
		lat1 *= Math.PI / 180;
		long1 *= Math.PI / 180;
		long2 *= Math.PI / 180;

		double S = (y / (earthRadius * 1000));
		double A = Math.cos(S);
		double B = Math.cos(lat1);
		double C = Math.cos(deltaLamda);
		double E = Math.tan(lat1);

		return 2 * (Math
				.atan((Math.sqrt(Math.pow(Math.cos(A), 2) * Math.pow(Math.cos(deltaLamda), 2) + Math.pow(Math.sin(A), 2)
						- Math.pow(Math.cos(S), 2)) + Math.sin(A)) / (Math.cos(A) * Math.cos(deltaLamda) + Math.cos(S)))
				+ Math.PI * n);
//		double lat1 = topLeftMap.y; 
//		double long1 = topLeftMap.x; 
//		double lat2 = latitude;
//		double long2 = topLeftMap.x;
//		
//		// convert to degrees
//		lat1 *= Math.PI / 180;
//		long1 *= Math.PI / 180;
//		lat2 *= Math.PI / 180;
//		long2 *= Math.PI / 180;
//
//		double deltaLamda = absDiffRad(long1, long2);
//		// gets the distance exactly as a float the shifts it 2 places to the right
//		// then rounds it to the nearest long, then shifts it 2 places to the left
//		// meaning it rounds it to the nearest 100.
//		double deltaSigma = Math
//				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLamda));
//		return earthRadius * deltaSigma * 1000;		
//				
//				A/B=E*sin(G)+C*cos(g)

	}

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}
}
