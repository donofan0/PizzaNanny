import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
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
	final static double earthRadius = 6371.0070072;
	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.4115);
	final static Point bottomRigthMap = new Point(longitudeToX(-6.455), latitudeToY(53.2944));
	final static Point apachePizza = new Point(longitudeToX(-6.592963), latitudeToY(53.381176));
	static double scaleFactor = 1.0;

	Map(Rectangle frame) {
		JPanel mapImagePanel = new JPanel();

		BufferedImage myPicture = null;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(frame.width, -1, Image.SCALE_SMOOTH)));
		picLabel.setBounds(frame);
		mapImagePanel.setBounds(frame);
		mapImagePanel.add(picLabel, 0);

		Points points = new Points();
		points.setPreferredSize(frame.getSize());
		points.setBounds(frame);
		points.setOpaque(false);

		Lines lines = new Lines();
		lines.setPreferredSize(frame.getSize());
		lines.setBounds(frame);
		lines.setOpaque(false);

		this.setBounds(frame);
		this.add(mapImagePanel, JLayeredPane.DEFAULT_LAYER);
		this.add(points, JLayeredPane.PALETTE_LAYER);
		this.add(lines, JLayeredPane.MODAL_LAYER);
	}

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}

	public static double calculateDistance(Point in1, Point in2) {
		return Math.sqrt(Math.pow((in2.x - in1.x), 2) + Math.pow((in2.y - in1.y), 2));
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

	public static Point latLongtoPoint(double latitude, double longitude) {
		Point out = new Point(longitudeToX(longitude), latitudeToY(latitude));
		return out;
	}

	public static int latitudeToY(double latitude) {
		return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
	}

	public static int longitudeToX(double longitude) {
		return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
	}
}

class Points extends JComponent {
	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		// scale factors = pixels per meter
		Map.scaleFactor = (double) g2.getClipBounds().width / Map.bottomRigthMap.x;
		for (Customer customer : Main.customers) {
			int x = (int) Math.round(customer.location.x * Map.scaleFactor);
			int y = (int) Math.round(customer.location.y * Map.scaleFactor);
			g2.fillOval(x - 5, y - 5, 10, 10);
			// g2.drawOval(x, y, 5, 5);
		}

		int x = (int) Math.round(Map.apachePizza.x * Map.scaleFactor);
		int y = (int) Math.round(Map.apachePizza.y * Map.scaleFactor);
		g2.setColor(Color.RED);
		g2.fillOval(x - 5, y - 5, 10, 10);
	}
}

class Lines extends JComponent {
	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		for (int i = 0; i < Main.bestPath.size(); i++) {
			Point start = Map.apachePizza;
			Point end = Main.customers.get(Main.bestPath.get(i)).location;
			if (i != 0) {
				start = Main.customers.get(Main.bestPath.get(i - 1)).location;
				end = Main.customers.get(Main.bestPath.get(i)).location;
			}

			g2.setColor(Color.BLUE);
			g2.draw(new Line2D.Double(start.x * Map.scaleFactor, start.y * Map.scaleFactor, end.x * Map.scaleFactor,
					end.y * Map.scaleFactor));
		}
	}
}