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
	final static double earthRadius = 6371.0070072;
	final static Point2D.Double topLeftMap = new Point2D.Double(-6.714, 53.411);
	final static Point bottomRigthMap = new Point(longitudeToX(-6.4546), latitudeToY(53.2857));
	final static Point apachePizza = new Point(longitudeToX(-6.59296), latitudeToY(53.381176));
	final static float aspectRatio = 1.23672055427f;
	final static int driverSpeed = 1000; // meters per minute
	static boolean constrainWidth = true;

	Map(Rectangle frame) {
		JPanel mapImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		if (frame.width > frame.height * aspectRatio) {
			constrainWidth = false;
		} else {
			constrainWidth = true;
		}

		BufferedImage myPicture = null;
		JLabel picLabel;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (constrainWidth) {
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(frame.width, -1, Image.SCALE_SMOOTH)));
		} else {
			picLabel = new JLabel(new ImageIcon(myPicture.getScaledInstance(-1, frame.height, Image.SCALE_SMOOTH)));
		}
		picLabel.setBounds(frame);
		mapImagePanel.setBounds(frame);
		mapImagePanel.add(picLabel, 0);

		Lines lines = new Lines();
		lines.setPreferredSize(frame.getSize());
		lines.setBounds(frame);
		lines.setOpaque(false);

		Points points = new Points();
		points.setPreferredSize(frame.getSize());
		points.setBounds(frame);
		points.setOpaque(false);

		this.setBounds(frame);
		this.add(mapImagePanel, JLayeredPane.DEFAULT_LAYER);
		this.add(lines, JLayeredPane.PALETTE_LAYER);
		this.add(points, JLayeredPane.MODAL_LAYER);

		if (ControlPanel.droneRunning) {
			Drone drone = new Drone(frame.getSize());
			drone.setPreferredSize(frame.getSize());
			drone.setBounds(frame);
			drone.setOpaque(false);
			this.add(drone, JLayeredPane.POPUP_LAYER);
		}
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
		double[] distances = CalculateTotalDistance(Main.bestPath.subList(0, pathIndex + 1));
		float time = (float) (distances[distances.length - 1] / driverSpeed) + curCustomer.waitTime;
		return time;
	}

	// returns an array of where element 1 is the time late and element 2 is the
	// total distance of the path
	public static double[] calculateTimeDistance(ArrayList<Integer> path) {
		double[] distances = CalculateTotalDistance(path);

		float lateMins = 0;
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			float time = (float) (distances[i] / driverSpeed) + endCustomer.waitTime;
			if (time > 30) {
				lateMins += time - 30;
			}
		}

		double[] output = { lateMins, distances[distances.length - 1] };
		return output;
	}

	public static double[] CalculateTotalDistance(List<Integer> path) {
		double[] distances = new double[path.size()];
		for (int i = 0; i < path.size(); i++) {
			Customer endCustomer = Main.customers.get(path.get(i));
			Point start = Map.apachePizza;
			// start = endCustomer.location; //TODO Remove
			Point end = endCustomer.location;
			if (i != 0) {
				start = Main.customers.get(path.get(i - 1)).location;
				end = Main.customers.get(path.get(i)).location;
			}
			double distance = Map.calculateDistance(start, end);
			if (i == 0) {
				distances[i] = distance;
			} else {
				distances[i] = distance + distances[i - 1];
			}
		}
		return distances;
	}
}

class Points extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1035389041198415412L;
	double scaleFactor;

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		// scale factors = pixels per meter
		if (Map.constrainWidth) {
			scaleFactor = (double) g2.getClipBounds().width / Map.bottomRigthMap.x;
		} else {
			scaleFactor = (double) g2.getClipBounds().height / Map.bottomRigthMap.y;
		}

		for (int i = 0; i < Main.customers.size(); i++) {
			Customer customer = Main.customers.get(i);
			int x = (int) Math.round(customer.location.x * scaleFactor);
			int y = (int) Math.round(customer.location.y * scaleFactor);
			if (ControlPanel.showEmoji) {
				g2.drawImage(Images.getEmoji(Map.calculateTimeWaiting(customer, Main.bestPath.indexOf(i))), x - 8,
						y - 8, 16, 16, null);
			} else {
				g2.fillOval(x - 5, y - 5, 10, 10);
			}

			g2.setFont(getFont().deriveFont(10f));
			g2.drawString("" + customer.id, x + 8, y - 3);
		}

		int x = (int) Math.round(Map.apachePizza.x * scaleFactor);
		int y = (int) Math.round(Map.apachePizza.y * scaleFactor);
		g2.setColor(Color.RED);
		g2.fillOval(x - 5, y - 5, 10, 10);
	}
}

class Lines extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2255340471176749645L;
	double scaleFactor;

	@Override
	public void paint(Graphics g) {
		// Draw a simple line using the Graphics2D draw() method.
		Graphics2D g2 = (Graphics2D) g;

		// scale factors = pixels per meter
		if (Map.constrainWidth) {
			scaleFactor = (double) g2.getClipBounds().width / Map.bottomRigthMap.x;
		} else {
			scaleFactor = (double) g2.getClipBounds().height / Map.bottomRigthMap.y;
		}

		for (int i = 0; i < Main.bestPath.size(); i++) {
			Point start = Map.apachePizza;
			// start = Main.customers.get(Main.bestPath.get(i)).location; //TODO Remove
			Point end = Main.customers.get(Main.bestPath.get(i)).location;
			if (i != 0) {
				start = Main.customers.get(Main.bestPath.get(i - 1)).location;
				end = Main.customers.get(Main.bestPath.get(i)).location;
			}

			g2.setColor(Color.BLUE);
			g2.draw(new Line2D.Double(start.x * scaleFactor, start.y * scaleFactor, end.x * scaleFactor,
					end.y * scaleFactor));
		}
	}
}

class Drone extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2255340493176749645L;
	final int droneSpeedScale = 60;
	double scaleFactor;
	JLabel drone;

	Drone(Dimension size) {
		// scale factors = pixels per meter
		if (Map.constrainWidth) {
			scaleFactor = size.getWidth() / Map.bottomRigthMap.x;
		} else {
			scaleFactor = size.getHeight() / Map.bottomRigthMap.y;
		}

		Point start = Map.apachePizza;
		Point end = Main.customers.get(Main.bestPath.get(0)).location;
		drone = new JLabel(new ImageIcon(Images.drone2Pizza));
		drone.setSize(60, 60);
		drone.setVisible(true);
		this.add(drone);
		doAnimation(new Rectangle2D.Double(start.x * scaleFactor - 30, start.y * scaleFactor - 30, 60, 60),
				new Point2D.Double(end.x * scaleFactor - 30, end.y * scaleFactor - 30),
				Map.calculateDistance(start, end), 0);
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
				Gui.map.repaint();
			}

			@Override
			protected void done() {
				// this method is called when the background
				// thread finishes execution
				System.out.println("Finished");

				if (curIndex + 1 < Main.bestPath.size() && ControlPanel.droneRunning) {
					Point startNew = Main.customers.get(Main.bestPath.get(curIndex)).location;
					start.setFrame(startNew, getSize());
					start.x = start.x * scaleFactor - start.width / 2;
					start.y = start.y * scaleFactor - start.height / 2;

					Point endNew = Main.customers.get(Main.bestPath.get(curIndex + 1)).location;
					end.x = endNew.x * scaleFactor - start.width / 2;
					end.y = endNew.y * scaleFactor - start.height / 2;

					doAnimation(start, end, Map.calculateDistance(startNew, endNew), curIndex + 1);
				} else {
					drone.setVisible(false);
				}
			}
		};

		// executes the swingworker on worker thread
		animate.execute();
	}
}