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
	// map GPS coordinates
	public final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.4105);
	public final static Point2D.Double bottomRigthMapGPS = new Point2D.Double(-6.4546, 53.2857);
	// coordinates in my coordinate system
	public final static Point bottomRigthMap = new Point(longitudeToX(-6.4546), latitudeToY(53.2857));
	public final static Point apachePizza = new Point(longitudeToX(-6.59296), latitudeToY(53.381176));
	public final static double earthRadius = 6371.0070072;
	public final static int driverSpeed = 1000; // meters per minute
	public final static float aspectRatio = 1.23672055427f; // aspect ratio of image = 1.23672055427:1

	static double scaleFactor; // pixels per meters

	private JPanel mapImagePanel;
	private Points points;
	private Lines lines;
	private Drone drone;
	private Rectangle mapSize;

	// Initializes the map
	Map(Rectangle frame) {
		mapImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		lines = new Lines();
		points = new Points();

		reSizeMap(frame);

		this.add(mapImagePanel, JLayeredPane.DEFAULT_LAYER);
		this.add(lines, JLayeredPane.PALETTE_LAYER);
		this.add(points, JLayeredPane.MODAL_LAYER);
	}

	// updates the points
	public void drawPoints() {
		points.setBounds(mapSize);
		points.repaint();
	}

	// updates the lines
	public void drawLines() {
		lines.setBounds(mapSize);
		lines.repaint();
	}

	// removes the old drone JPanel and adds the new one
	public void startDrone() {
		if (TSP.bestPath.length < 1) {
			return;
		}

		drone = new Drone(mapSize.getSize());
		drone.setBounds(mapSize);
		drone.setOpaque(false);

		this.remove(drone);
		this.add(drone, JLayeredPane.POPUP_LAYER);
	}

	// changes the scale of the map
	public void reSizeMap(Rectangle frame) {
		mapSize = frame;
		this.setBounds(mapSize);

		// removes the old image
		mapImagePanel.removeAll();

		// fetch the image
		BufferedImage myPicture = null;
		JLabel picLabel;
		try {
			myPicture = ImageIO.read(new File("map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// either scales the image to the width or height of the frame
		// depending on weather the limiting factor is width or height
		// based on the apectRatio
		if (mapSize.width > mapSize.height * aspectRatio) {
			// height based
			scaleFactor = (double) mapSize.height / Map.bottomRigthMap.y;
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(-1, this.getHeight(), Image.SCALE_SMOOTH)));
		} else {
			// width based
			scaleFactor = (double) mapSize.width / Map.bottomRigthMap.x;
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(this.getWidth(), -1, Image.SCALE_SMOOTH)));
		}

		// adds the new image
		mapImagePanel.setBounds(mapSize);
		mapImagePanel.add(picLabel, 0);

		// updates the points and lines
		drawPoints();
		drawLines();
	}

	/*
	 * Map Utilities
	 */

	// returns the distance between the provided GPS coordinates
	public static double calcGPSDistance(double lat1, double long1, double lat2, double long2) {
		// convert to degrees
		lat1 *= Math.PI / 180;
		long1 *= Math.PI / 180;
		lat2 *= Math.PI / 180;
		long2 *= Math.PI / 180;

		double deltaLamda = long1 - long2;
		// gets the distance exactly as a float the shifts it 2 places to the right
		// then rounds it to the nearest long, then shifts it 2 places to the left
		// meaning it rounds it to the nearest 100.
		double deltaSigma = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(deltaLamda));
		return earthRadius * deltaSigma * 1000;
	}

	// converts the provided GPS coordinates to a point on my coordinate system
	public static Point latLongtoPoint(double latitude, double longitude) {
		Point out = new Point(longitudeToX(longitude), latitudeToY(latitude));
		return out;
	}

	// converts the provided GPS longitude to a x coordinate on my coordinate system
	public static int longitudeToX(double longitude) {
		if (longitude < topLeftMap.x) {
			return (int) -Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
		} else {
			return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, topLeftMap.y, longitude));
		}
	}

	// converts the provided GPS latitude to a y coordinate on my coordinate system
	public static int latitudeToY(double latitude) {
		if (latitude > topLeftMap.y) {
			return (int) -Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
		} else {
			return (int) Math.round(calcGPSDistance(topLeftMap.y, topLeftMap.x, latitude, topLeftMap.x));
		}
	}

	// returns the provided point scaled from meters to pixels based on the scale
	// the point at (1000m,1000m) -> (10,10) pixels on the screen, scalefactor=0.01
	public static Point scalePoint(Point point) {
		return new Point((int) Math.round(point.x * scaleFactor), (int) Math.round(point.y * scaleFactor));
	}

	// returns the slope of the line joining the two points provided
	public static double calculateSlope(Point in1, Point in2) {
		if (in1.x - in2.x == 0) {
			return 999999999;
		}
		return (in1.getY() - in2.getY()) / (in1.getX() - in2.getX());
	}

}

class Points extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1035389041198415412L;

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// go through each customer,
		// find and scale its location then draw either a black circle
		// or a emoji in the corresponding coordinate,
		// then write the id of the point in the top right of the point
		for (int i = 0; i < TSP.customers.length; i++) {
			Customer customer = TSP.customers[i];
			Point point = Map.scalePoint(customer.location);
			if (ControlPanel.showEmoji) {
				float time = Algorithms.calculateTimeWaiting(customer, ArrayUtils.pathFindIndex(TSP.bestPath, i));
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

		// draw a point representing apache pizza in red
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
		Graphics2D g2 = (Graphics2D) g;

		// go through each customer in the bestpath
		for (int i = 0; i < TSP.bestPath.length; i++) {
			Point start;
			Point end;
			if (i == 0) {
				// first line from apache to the start
				start = Map.scalePoint(Map.apachePizza);
				end = Map.scalePoint(TSP.customers[TSP.bestPath[i]].location);
			} else {
				// Previous customer to this customer
				start = Map.scalePoint(TSP.customers[TSP.bestPath[i - 1]].location);
				end = Map.scalePoint(TSP.customers[TSP.bestPath[i]].location);
			}

			// draw the line in blue
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

	final int droneSpeedScale = 100; // the drone runs at 100 times real speed
	JLabel drone;

	// starts drone
	Drone(Dimension size) {
		// first travel move from Apache pizza to first customer
		Point start = Map.apachePizza;
		Point end = TSP.customers[TSP.bestPath[0]].location;

		// loads the drone picture
		drone = new JLabel(new ImageIcon(Images.drone2Pizza));
		drone.setSize(60, 60);
		this.add(drone);
		drone.setVisible(true);

		// calls the asynchronous recursive animation routine
		doAnimation(new Rectangle2D.Double(start.x * Map.scaleFactor - 30, start.y * Map.scaleFactor - 30, 60, 60), new Point2D.Double(end.x * Map.scaleFactor - 30, end.y * Map.scaleFactor - 30), start.distance(end), 0);
	}

	// asynchronous recursive routine which will start the drone at start
	// then moves it once every ms by xDelta and yDelta
	// this gives the effect of it flying
	// meaning it goes from start to end in a straight line
	// meaning from customer 0 to 1 in a line
	// when it is finished it will call itself again with the new coordinates
	// for example customer 1 to 2

	// start and end are the coordinates in pixels
	// distance between the points in meters is used for calculating speed
	// curIndex is used for the recursion
	private void doAnimation(final Rectangle2D.Double start, final Point2D.Double end, final double distance, final int curIndex) {
		drone.setBounds(start.getBounds());
		SwingWorker<Boolean, Rectangle2D.Double> animate = new SwingWorker<Boolean, Rectangle2D.Double>() {
			// the number of pixels it moves every ms in the x direction
			private double xDelta = (end.x - start.x) / ((distance * 60 * 1000) / (Map.driverSpeed * droneSpeedScale));
			// the number of pixels it moves every ms in the y direction
			private double yDelta = (end.y - start.y) / ((distance * 60 * 1000) / (Map.driverSpeed * droneSpeedScale));

			// stops the loop when it as reached the end
			private boolean running = true;

			// changes the start coordinates into a double so that the location can be
			// calculated more accurately
			private Rectangle2D.Double curLoc = new Rectangle2D.Double(start.getX(), start.getY(), start.getWidth(), start.getHeight());

			// calculates the coordinates on a new thread
			@Override
			protected Boolean doInBackground() throws Exception {
				// in case there is two points at the same coordinates
				// so distance is zero
				// so xdelta has a divided by zero therefore NaN(Not a Number)
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
					publish(curLoc); // sends the data to processing
				}

				return true;
			}

			@Override
			protected void process(List<Rectangle2D.Double> chunks) {
				/*
				 * sets the label to its new coordinates and size this is called in batches as for example it could
				 * execute like this doInBackground,doInBackground,doInBackground,doInBackground, process,
				 * process,doInBackground, process, process, process that is why it has to get the next item in the
				 * list
				 */
				Rectangle2D.Double val = (java.awt.geom.Rectangle2D.Double) chunks.get(chunks.size() - 1);
				drone.setBounds(val.getBounds());
				// Gui.map.repaint();
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution

				if (curIndex + 1 < TSP.bestPath.length && ControlPanel.droneRunning) {
					// calls itself with the next customer coordinates
					Point startNew = TSP.customers[TSP.bestPath[curIndex]].location;
					start.setFrame(startNew, getSize());
					start.x = start.x * Map.scaleFactor - start.width / 2;
					start.y = start.y * Map.scaleFactor - start.height / 2;

					Point endNew = TSP.customers[TSP.bestPath[curIndex + 1]].location;
					end.x = endNew.x * Map.scaleFactor - start.width / 2;
					end.y = endNew.y * Map.scaleFactor - start.height / 2;

					doAnimation(start, end, startNew.distance(endNew), curIndex + 1);
				} else {
					// recursion over
					drone.setVisible(false);
					ControlPanel.droneRunning = false;
					ControlPanel.startDrone.setText("Start Drone");
				}
			}
		};

		// executes the swing worker on worker thread
		animate.execute();
	}
}