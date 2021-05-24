package pointsMaker;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.Scanner;

public class MainGenerator {

	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.411);
	final static Point2D.Double bottomRigthMap = new Point2D.Double(-6.4546, 53.2857);
	
	public static void main(String[] args) {
		try (Scanner scan = new Scanner(System.in)) {
			Random rand = new Random();

			int maxPoints = scan.nextInt();
			for (int i=0;i<maxPoints;i++) {
				double x = rand.nextFloat()*(bottomRigthMap.x - topLeftMap.x) + topLeftMap.x;
				double y = rand.nextFloat()*(bottomRigthMap.y - topLeftMap.y) + topLeftMap.y;
				int time = rand.nextInt(31);
				System.out.println((i+1)+",This is a test address ,"+time+","+y+","+x);
			}
		}
	}
}
