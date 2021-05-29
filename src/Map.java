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
	final static Point2D.Double bottomRigthMapGPS = new Point2D.Double(-6.4546, 53.2857);
	final static Point bottomRigthMap = new Point(longitudeToX(bottomRigthMapGPS.x), latitudeToY(bottomRigthMapGPS.y));
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
		if (Main.customers.length < 1) {
			return;
		}

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
			myPicture = ImageIO.read(new File("map.png"));
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
		if (latitude > topLeftMap.y) {
			return (int) -Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
		} else {
			return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
		}
	}

	public static int longitudeToX(double longitude) {
		if (longitude < topLeftMap.x) {
			return (int) -Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
		} else {
			return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
		}
	}

	public static float calculateTimeWaiting(Customer curCustomer, int pathIndex) {
		if (Main.bestPath.length < 1 || pathIndex < 0) {
			return -1;
		}

		double[] distances = CalculateTotalDistance(bestPathSubList(pathIndex + 1));
		float time = (float) (distances[distances.length - 1] / driverSpeed) + curCustomer.waitTime;
		return time;
	}

	// returns an array of where element 1 is the time late and element 2 is the
	// total distance of the path
	public static double[] calculateTimeDistance(int[] path) {
		double[] distances = CalculateTotalDistance(path);

		double lateMins = 0;
		for (int i = 0; i < path.length; i++) {
			Customer endCustomer = Main.customers[path[i]];
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

	public static double calculateTime(int[] path) {
		return calculateTimeDistance(path)[0];
	}

	public static double calculateTime() {
		return calculateTimeDistance(Main.bestPath)[0];
	}

	public static double[] CalculateTotalDistance(int[] path) {
		double[] distances = new double[path.length];
		for (int i = 0; i < path.length; i++) {
			Customer endCustomer = Main.customers[path[i]];
			Point start = Map.apachePizza;
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers[path[i - 1]].location;
				end = Main.customers[path[i]].location;
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

	public static boolean bestPathContains(int in) {
		for (int i = 0; i < Main.bestPath.length; i++) {
			if (Main.bestPath[i] == in) {
				return true;
			}
		}
		return false;
	}

	public static boolean pathContains(int[] path, int in) {
		for (int i = 0; i < path.length; i++) {
			if (path[i] == in) {
				return true;
			}
		}
		return false;
	}

	public static int[] bestPathSubList(int size) {
		int[] output = new int[size];
		for (int i = 0; i < size; i++) {
			output[i] = Main.bestPath[i];
		}
		return output;
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

	public static int[] bestPathInsert(int index, int value) {
		int[] newBestPath = new int[Main.customers.length];
		for (int i = 0; i < newBestPath.length; i++) {
			if (i < index) {
				newBestPath[i] = Main.bestPath[i];
			} else if (i == index) {
				newBestPath[i] = value;
			} else {
				newBestPath[i] = Main.bestPath[i - 1];
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

	public static void bestPathRemove(int value) {
		int[] newBestPath = new int[Main.bestPath.length - 1];
		boolean found = false;
		for (int i = 0; i < newBestPath.length; i++) {
			if (!found && Main.bestPath[i] == value) {
				found = true;
			}

			if (found) {
				newBestPath[i] = Main.bestPath[i + 1];
			} else {
				newBestPath[i] = Main.bestPath[i];
			}
		}
		Main.bestPath = newBestPath;
	}

	public static int bestPathFindIndex(int id) {
		for (int i = 0; i < Main.bestPath.length; i++) {
			if (Main.bestPath[i] == id) {
				return i;
			}
		}
		return -1;
	}

	public static Point scalePoint(Point point) {
		return new Point((int) Math.round(point.x * scaleFactor), (int) Math.round(point.y * scaleFactor));
	}

	public static int getCustomerClicked(Point point) {
		int nearestCustomerIndex = 0;
		double nearestDistance = 999999999;
		for (int i = 0; i < Main.customers.length; i++) {
			Customer customer = Main.customers[i];
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

		for (int i = 0; i < Main.customers.length; i++) {
			Customer customer = Main.customers[i];
			Point point = Map.scalePoint(customer.location);
			if (ControlPanel.showEmoji) {
				float time = Map.calculateTimeWaiting(customer, Map.bestPathFindIndex(i));
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

		for (int i = 0; i < Main.bestPath.length; i++) {

			Point start;
			Point end;
			if (i == 0) {
				start = Map.scalePoint(Map.apachePizza);
				end = Map.scalePoint(Main.customers[Main.bestPath[i]].location);
			} else {
				start = Map.scalePoint(Main.customers[Main.bestPath[i - 1]].location);
				end = Map.scalePoint(Main.customers[Main.bestPath[i]].location);
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
		Point end = Main.customers[Main.bestPath[0]].location;
		drone = new JLabel(new ImageIcon(Images.drone2Pizza));
		drone.setSize(60, 60);
		this.add(drone);
		drone.setVisible(true);
		doAnimation(new Rectangle2D.Double(start.x * Map.scaleFactor - 30, start.y * Map.scaleFactor - 30, 60, 60),
				new Point2D.Double(end.x * Map.scaleFactor - 30, end.y * Map.scaleFactor - 30), start.distance(end), 0);
	}

	private void doAnimation(final Rectangle2D.Double start, final Point2D.Double end, final double distance,
			final int curIndex) {
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

				if (curIndex + 1 < Main.bestPath.length && ControlPanel.droneRunning) {
					Point startNew = Main.customers[Main.bestPath[curIndex]].location;
					start.setFrame(startNew, getSize());
					start.x = start.x * Map.scaleFactor - start.width / 2;
					start.y = start.y * Map.scaleFactor - start.height / 2;

					Point endNew = Main.customers[Main.bestPath[curIndex + 1]].location;
					end.x = endNew.x * Map.scaleFactor - start.width / 2;
					end.y = endNew.y * Map.scaleFactor - start.height / 2;

					doAnimation(start, end, startNew.distance(endNew), curIndex + 1);
				} else {
					drone.setVisible(false);
					ControlPanel.droneRunning = false;
					ControlPanel.startDrone.setText("Start Drone");
				}
			}
		};

		// executes the swingworker on worker thread
		animate.execute();
	}
}