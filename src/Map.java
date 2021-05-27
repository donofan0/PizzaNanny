import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class Map extends JLayeredPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2755569004426869297L;

	private JPanel mapImagePanel;
	private Points points;
	private Lines lines;
	private Drone drone;
	private Rectangle mapSize;

	final static double earthRadius = 6371.0070072;

	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.4105);
	final static Point bottomRigthMap = new Point(longitudeToX(-6.4546), latitudeToY(53.2857));
	final static Point apachePizza = new Point(longitudeToX(-6.59296), latitudeToY(53.381176));
	final static int driverSpeed = 1000; // meters per minute

	final static float aspectRatio = 1.23672055427f; // aspect ratio of the map decides when to constrain width
	static double scaleFactor;

	Map(Rectangle frame) {
		mapImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lines = new Lines();
		points = new Points();

		reSizeMap(frame);

		this.add(mapImagePanel, JLayeredPane.DEFAULT_LAYER);
		this.add(lines, JLayeredPane.PALETTE_LAYER);
		this.add(points, JLayeredPane.MODAL_LAYER);
	}

	public void drawPoints() {
		points.setBounds(mapSize);
		points.repaint();
	}

	public void drawLines() {
		lines.setBounds(mapSize);
		lines.repaint();
	}

	public void startDrone() {
		drone = new Drone(mapSize.getSize());
		drone.setBounds(mapSize);
		drone.setOpaque(false);

		this.remove(drone);
		this.add(drone, JLayeredPane.POPUP_LAYER);
	}

	public void reSizeMap(Rectangle frame) {
		mapSize = frame;
		this.setBounds(mapSize);

		mapImagePanel.removeAll();
		BufferedImage myPicture = null;
		JLabel picLabel;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mapSize.width > mapSize.height * aspectRatio) {
			// height based
			scaleFactor = (double) mapSize.height / Map.bottomRigthMap.y;
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(-1, this.getHeight(), Image.SCALE_SMOOTH)));
		} else {
			// width based
			scaleFactor = (double) mapSize.width / Map.bottomRigthMap.x;
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(this.getWidth(), -1, Image.SCALE_SMOOTH)));
		}

		mapImagePanel.setBounds(mapSize);
		mapImagePanel.add(picLabel, 0);

		drawPoints();
		drawLines();
	}

	public static double absDiffRad(double in1, double in2) {
		double diff = (in1 - in2);
		if (diff < 0) {
			diff = -diff;
		}
		return diff;
	}

	public static double calculateSlope(Point in1, Point in2) {
		if (in1.x - in2.x == 0) {
			return 999999999;
		}
		return (in1.getY() - in2.getY()) / (in1.getX() - in2.getX());
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

	public static float calculateTimeWaiting(Customer curCustomer, int pathIndex) {
		if (Main.bestPath.isEmpty() || pathIndex < 0) {
			return -1;
		}

		double[] distances = CalculateTotalDistance(Main.bestPath.subList(0, pathIndex + 1));
		float time = (float) (distances[distances.length - 1] / driverSpeed) + curCustomer.waitTime;
		return time;
	}

	// returns an array of where element 1 is the time late and element 2 is the
	// total distance of the path
	public static double[] calculateTimeDistance(ArrayList<Integer> path) {
		double[] distances = CalculateTotalDistance(path);

		double lateMins = 0;
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			double time = (distances[i] / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		double[] output = { lateMins, distances[distances.length - 1] };
		return output;
	}

	public static double[] calculateTimeDistance() {
		return calculateTimeDistance(Main.bestPath);
	}

	public static double calculateTime(ArrayList<Integer> path) {
		return calculateTimeDistance(path)[0];
	}

	public static double calculateTime() {
		return calculateTimeDistance(Main.bestPath)[0];
	}

	public static float calculateTimeUpTo(int[] path, float max) {
		float lastDistance = 0;
		float lateMins = 0;
		for (int i = 0; i < path.length; i++) {
			Customer endCustomer = Main.customers.get(path[i]);
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers.get(path[i - 1]).location;
				end = Main.customers.get(path[i]).location;
			}

			float distance = (float) start.distance(end);
			distance += lastDistance;
			lastDistance = distance;

			double time = (distance / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}

			if (lateMins >= max) {
				return lateMins;
			}
		}
		return lateMins;
	}

	public static double[] CalculateTotalDistance(List<Integer> path) {
		double[] distances = new double[path.size()];
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers.get(path.get(i - 1)).location;
				end = Main.customers.get(path.get(i)).location;
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

	public static Point scalePoint(Point point) {
		return new Point((int) Math.round(point.x * scaleFactor), (int) Math.round(point.y * scaleFactor));
	}

	public static int getCustomerClicked(Point point) {
		int nearestCustomerIndex = 0;
		double nearestDistance = 999999999;
		for (int i = 0; i < Main.customers.size(); i++) {
			Customer customer = Main.customers.get(i);
			double distance = point.distance(scalePoint(customer.location));
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestCustomerIndex = i;
			}
		}
		return nearestCustomerIndex;
	}
}

class Points extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1035389041198415412L;

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		for (int i = 0; i < Main.customers.size(); i++) {
			Customer customer = Main.customers.get(i);
			Point point = Map.scalePoint(customer.location);
			if (ControlPanel.showEmoji) {
				float time = Map.calculateTimeWaiting(customer, Main.bestPath.indexOf(i));
				if (time >= 0) {
					g2.drawImage(Images.getEmoji(time), point.x - 8, point.y - 8, 16, 16, null);
				} else {
					g2.fillOval(point.x - 5, point.y - 5, 10, 10);
				}
			} else {
				g2.fillOval(point.x - 5, point.y - 5, 10, 10);
			}

			g2.setFont(getFont().deriveFont(10f));
			g2.drawString("" + customer.id, point.x + 8, point.y - 3);
		}

		Point point = Map.scalePoint(Map.apachePizza);
		g2.setColor(Color.RED);
		g2.fillOval(point.x - 5, point.y - 5, 10, 10);
	}
}

class Lines extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2255340471176749645L;

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		for (int i = 0; i < Main.bestPath.size(); i++) {

			Point start;
			Point end;
			if (i == 0) {
				start = Map.scalePoint(Map.apachePizza);
				end = Map.scalePoint(Main.customers.get(Main.bestPath.get(i)).location);
			} else {
				start = Map.scalePoint(Main.customers.get(Main.bestPath.get(i - 1)).location);
				end = Map.scalePoint(Main.customers.get(Main.bestPath.get(i)).location);
			}

			g2.setColor(Color.BLUE);
			g2.draw(new Line2D.Double(start.x, start.y, end.x, end.y));
		}
	}
}

class Drone extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2255340493176749645L;
	final int droneSpeedScale = 60 * 10;
	JLabel drone;

	Drone(Dimension size) {

		Point start = Map.apachePizza;
		Point end = Main.customers.get(Main.bestPath.get(0)).location;
		drone = new JLabel(new ImageIcon(Images.drone2Pizza));
		drone.setSize(60, 60);
		this.add(drone);
		drone.setVisible(true);
		doAnimation(new Rectangle2D.Double(start.x * Map.scaleFactor - 30, start.y * Map.scaleFactor - 30, 60, 60),
				new Point2D.Double(end.x * Map.scaleFactor - 30, end.y * Map.scaleFactor - 30), start.distance(end), 0);
	}

	private void doAnimation(Rectangle2D.Double start, Point2D.Double end, double distance, int curIndex) {
		drone.setBounds(start.getBounds());
		SwingWorker<Boolean, Rectangle2D.Double> animate = new SwingWorker<Boolean, Rectangle2D.Double>() {
			private double xDelta = (end.x - start.x) / ((distance * 60 * 1000) / (Map.driverSpeed * droneSpeedScale));
			private double yDelta = (end.y - start.y) / ((distance * 60 * 1000) / (Map.driverSpeed * droneSpeedScale));
			private boolean running = true; // makes sure that the while loop does not start again

			// changes the start coordinates into a double so that the location can be
			// calculated more accurately
			private Rectangle2D.Double curLoc = new Rectangle2D.Double(start.getX(), start.getY(), start.getWidth(),
					start.getHeight());

			@Override
			protected Boolean doInBackground() throws Exception {
				if (Double.isNaN(xDelta) || Double.isNaN(yDelta)) {
					running = false;
				}

				while (running && ControlPanel.droneRunning) {
					boolean xDone = false; // records when it has reached its target x
					boolean yDone = false; // records when it has reached its target y

					if (((curLoc.getX() >= end.x) && (xDelta >= 0)) || ((curLoc.getX() <= end.x) && (xDelta <= 0))) {
						xDone = true;
					}

					if (((curLoc.getY() >= end.y) && (yDelta >= 0)) || ((curLoc.getY() <= end.y) && (yDelta <= 0))) {
						yDone = true;
					}

					if (xDone && yDone) {
						running = false;
						return running;
					}

					if (!xDone) {
						curLoc.x += xDelta;
					}
					if (!yDone) {
						curLoc.y += yDelta;
					}

					Thread.sleep(1);
					// System.out.println(curLoc.toString()+", xdelta="+xDelta+", ydelta="+yDelta);
					publish(curLoc); // sends the data to proccesing
				}

				return true;
			}

			@Override
			protected void process(List<Rectangle2D.Double> chunks) {
				/*
				 * sets the label to its new coordinates and size this is called in batches as
				 * in it could execute like this
				 * doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get
				 * the next item in the list
				 */
				Rectangle2D.Double val = (java.awt.geom.Rectangle2D.Double) chunks.get(chunks.size() - 1);
				drone.setBounds(val.getBounds());
				// Gui.map.repaint();
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution

				if (curIndex + 1 < Main.bestPath.size() && ControlPanel.droneRunning) {
					Point startNew = Main.customers.get(Main.bestPath.get(curIndex)).location;
					start.setFrame(startNew, getSize());
					start.x = start.x * Map.scaleFactor - start.width / 2;
					start.y = start.y * Map.scaleFactor - start.height / 2;

					Point endNew = Main.customers.get(Main.bestPath.get(curIndex + 1)).location;
					end.x = endNew.x * Map.scaleFactor - start.width / 2;
					end.y = endNew.y * Map.scaleFactor - start.height / 2;

					doAnimation(start, end, startNew.distance(endNew), curIndex + 1);
				} else {
					drone.setVisible(false);
				}
			}
		};

		// executes the swingworker on worker thread
		animate.execute();
	}
}