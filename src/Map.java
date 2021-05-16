import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class Map extends JLayeredPane {
	Map(int width, int height) {
		JPanel mapImagePanel = new JPanel();
		JPanel pointsPanel = new JPanel();

		this.add(mapImagePanel, JLayeredPane.DEFAULT_LAYER);
		this.add(pointsPanel, JLayeredPane.PALETTE_LAYER);

		BufferedImage myPicture = null;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		mapImagePanel.setBounds(0, 0, width, height);
		mapImagePanel.add(picLabel, 0);

		Points points = new Points();
		points.setPreferredSize(new Dimension(width, height));
		pointsPanel.setBounds(0, 0, width, height);
		pointsPanel.setOpaque(false);
		pointsPanel.add(points);
	}
}

class Points extends JComponent {
	final static double earthRadius = 6371.0070072;
	final static Point2D.Double topLeftMap = new Point2D.Double(-6.715558, 53.410977);
	final static Point bottomRigthMap = new Point(longitudeToX(-6.456531), latitudeToY(53.286957));
	final static Point apachePizza = new Point(longitudeToX(-6.592963), latitudeToY(53.381176));
	double scaleFactor = 1.0;

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}

	public static double calcDistance(double lat1, double long1, double lat2, double long2) {
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
		System.out.println(earthRadius * deltaSigma * 1000);
		return earthRadius * deltaSigma * 1000;
	}

	public static Point latLongtoPoint(double latitude, double longitude) {
		Point out = new Point(longitudeToX(longitude), latitudeToY(latitude));
		return out;
	}

	public static int latitudeToY(double latitude) {
		return (int) Math.round(calcDistance(topLeftMap.y, 0, latitude, 0));
	}

	public static int longitudeToX(double longitude) {
		return (int) Math.round(calcDistance(0, topLeftMap.x, 0, longitude));
	}

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		// scale factors = pixels per meter

		scaleFactor = (double) 800 / bottomRigthMap.y;
		for (Customer customer : Main.customers) {
			int x = (int) Math.round(customer.location.x * scaleFactor);
			int y = (int) Math.round(customer.location.y * scaleFactor);
			g2.fillOval(x, y, 8, 8);
			// g2.drawOval(x, y, 5, 5);
		}

		int x = (int) Math.round(apachePizza.x * scaleFactor);
		int y = (int) Math.round(apachePizza.y * scaleFactor);
		g2.setColor(Color.RED);
		g2.fillOval(x, y, 8, 8);

//		g2.setStroke(new BasicStroke(2f));
//		g2.setColor(Color.RED);
//		g2.draw(new Line2D.Double(50, 150, 250, 350));
//		g2.setColor(Color.GREEN);
//		g2.draw(new Line2D.Double(250, 350, 350, 250));
//		g2.setColor(Color.BLUE);
//		g2.draw(new Line2D.Double(350, 250, 150, 50));
//		g2.setColor(Color.YELLOW);
//		g2.draw(new Line2D.Double(150, 50, 50, 150));
//		g2.setColor(Color.BLACK);
//		g2.draw(new Line2D.Double(0, 0, 400, 400));

	}
}