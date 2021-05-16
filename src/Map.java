import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
		points.setPreferredSize(new Dimension(400, 400));
		pointsPanel.setBounds(0, 0, width, height);
		pointsPanel.setOpaque(false);
		pointsPanel.add(points);
	}
}

class Points extends JComponent {
	final static double earthRadius = 6371.0070072;
	double scaleFactor = 1.0;

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}

	public static double calcDistance(double lat1, double long1, double lat2, double long2) {
		double deltaLamda = absDiffRad(long1, long2);
		// gets the distance exactly as a float the shifts it 2 places to the right
		// then rounds it to the nearest long, then shifts it 2 places to the left
		// meaning it rounds it to the nearest 100.
		double deltaSigma = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLamda));
		return earthRadius * deltaSigma;
	}

	public static long latitudeToX(double latitude) {
		return Math.round(calcDistance(Main.origin.x, 0, latitude, 0));
	}

	public static long longitudeToY(double longitude) {
		return Math.round(calcDistance(0, Main.origin.y, 0, longitude));
	}

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		scaleFactor = (double) 400 / Main.largestDistance;
		for (Customer customer : Main.customers) {
			int x = (int) Math.round(customer.x * scaleFactor);
			int y = (int) Math.round(customer.y * scaleFactor);
			g2.fillOval(x, y, 5, 5);
			// g2.drawOval(x, y, 5, 5);
		}

		int x = (int) Math.round(Main.origin.x * scaleFactor);
		int y = (int) Math.round(Main.origin.y * scaleFactor);
		g2.setColor(Color.RED);
		g2.fillOval(x, y, 5, 5);

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