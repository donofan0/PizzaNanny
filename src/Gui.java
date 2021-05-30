import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Gui {
	public static Map map;
	public static JList algCompare; // the list of all the algorithms compared
	public static ControlPanel ctrlPanel;
	public static JTextArea inputTextArea;

	private final int ctrlWidth = 180;
	private final int inputBoxHeight = 200;
	private static final Dimension windowSize = new Dimension(1000, 890);

	private static JFrame generatorFrame;
	private static JFrame compareFrame;
	private static JSpinner numOfCustomersValue;
	private static JSpinner numOfClustersValue;
	private static JSpinner standDeviationValue;

	public Gui() {
		final JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(windowSize);
		frame.setVisible(true);

		Dimension frameSize = frame.getContentPane().getSize();

		// Initialize the map and the control panel with their sizes so that they can
		// scale the content correctly
		map = new Map(new Rectangle(0, 0, frameSize.width - ctrlWidth, frameSize.height - inputBoxHeight));
		ctrlPanel = new ControlPanel(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));

		// adds and manually positions the input text area
		inputTextArea = new JTextArea();
		final JScrollPane inputDeliverys = new JScrollPane(inputTextArea);
		inputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		inputDeliverys.setBounds(new Rectangle(0, frameSize.height - inputBoxHeight, frameSize.width - ctrlWidth,
				inputBoxHeight));

		frame.getContentPane().add(ctrlPanel);
		frame.getContentPane().add(inputDeliverys);
		frame.getContentPane().add(map);

		frame.validate();

		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				Dimension frameSize = frame.getContentPane().getSize();

				// move the input text box and the control panel to there new place
				ctrlPanel.setBounds(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));
				inputDeliverys.setBounds(new Rectangle(0, frameSize.height - inputBoxHeight,
						frameSize.width - ctrlWidth, inputBoxHeight));

				// resizes the map to its new size and adjusts its scale factor
				Rectangle mapDemensions = new Rectangle(0, 0, frameSize.width - ctrlWidth,
						frameSize.height - inputBoxHeight);
				map.reSizeMap(mapDemensions);

				frame.validate();
			}
		});

		// used for the edit mode to detect mouse clicks
		map.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!ctrlPanel.editMode) {
					return;
				}
				int customerID = getCustomerClicked(e.getPoint());
				if (!ArrayUtils.pathContains(TSP.bestPath, customerID)) {
					int[] newBestPath = new int[TSP.bestPath.length + 1];
					for (int i = 0; i < TSP.bestPath.length; i++) {
						newBestPath[i] = TSP.bestPath[i];
					}
					newBestPath[TSP.bestPath.length] = customerID;
					TSP.bestPath = newBestPath;
				} else {
					ArrayUtils.bestPathRemove(customerID);
				}

				ctrlPanel.drawOutput();

				map.drawPoints();
				map.drawLines();
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});
	}

	// opens the comparison window and makes sure there is only ever one
	public static void startComparisionWindow() {
		if (compareFrame != null) {
			compareFrame.dispose();
		}

		compareFrame = new JFrame("Algorithms Comparision");
		compareFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		compareFrame.pack();
		compareFrame.setSize(1100, 300);
		compareFrame.setVisible(true);

		JPanel comparePanel = new JPanel(new BorderLayout());

		// gets all the synchronous algorithm results
		String[] results = Algorithms.compareAlogrithemsWithResults();

		// creates a list with the algorithm results and makes it look like a table
		algCompare = new JList(results);
		algCompare.setLayoutOrientation(JList.VERTICAL);
		algCompare.setFont(new Font("monospaced", Font.BOLD, 16));
		algCompare.setSelectedIndex(1);

		// calls the algorithm which you click on so you can see the map
		algCompare.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() || ControlPanel.branchAlgorithmRunning
						|| ControlPanel.groupAlgorithmRunning) {
					return;
				}

				int selectedAlg = algCompare.getSelectedIndex() - 1;
				if (selectedAlg < 0 || selectedAlg > Algorithms.algorithms.length - 2) {
					return;
				}
				if (TSP.customers.length >= 13 && selectedAlg == Algorithms.algorithms.length - 2) {
					return;
				}

				// selects the algorithm on in the GUI and pushes submit
				ctrlPanel.algorithmSelect.setSelectedIndex(selectedAlg);
				ctrlPanel.submitButtonAction();
			}

		});

		// places the list in a scroll pane so if you shrink the window you can pan
		// Around the result
		JScrollPane listScroller = new JScrollPane(algCompare);
		listScroller.setPreferredSize(new Dimension(250, 80));
		comparePanel.add(listScroller, BorderLayout.CENTER);

		// adds the close button
		JButton close = new JButton("Close");
		comparePanel.add(close, BorderLayout.SOUTH);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				compareFrame.dispose();
			}
		});

		compareFrame.getContentPane().add(comparePanel);
		compareFrame.validate();
	}

	// opens the point generator window and makes sure there is only one open
	public static void startPointsGenerator() {
		if (generatorFrame != null) {
			generatorFrame.dispose();
		}

		generatorFrame = new JFrame("Points Generator");
		generatorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		generatorFrame.pack();
		generatorFrame.setSize(300, 200);
		generatorFrame.setVisible(true);

		JPanel comparePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Default constants
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(4, 4, 4, 4);
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;

		JLabel Label;
		SpinnerNumberModel spinModel;

		Label = new JLabel("Number of Customers:");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(Label, c);

		spinModel = new SpinnerNumberModel(100, 0, 100, 1);
		numOfCustomersValue = new JSpinner(spinModel);
		c.gridx = 1;
		comparePanel.add(numOfCustomersValue, c);

		Label = new JLabel("Number of Clusters:");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(Label, c);

		spinModel = new SpinnerNumberModel(5, 0, 99, 1);
		numOfClustersValue = new JSpinner(spinModel);
		c.gridx = 1;
		comparePanel.add(numOfClustersValue, c);

		Label = new JLabel("Standard Deviation:");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(Label, c);

		spinModel = new SpinnerNumberModel(1, 0, 100, 0.5);
		standDeviationValue = new JSpinner(spinModel);
		c.gridx = 1;
		comparePanel.add(standDeviationValue, c);

		JButton close = new JButton("Close");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(close, c);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generatorFrame.dispose();
			}
		});

		JButton generate = new JButton("Generate");
		c.gridx = 1;
		comparePanel.add(generate, c);
		generate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// generate the points
				int maxPoints = (Integer) numOfCustomersValue.getValue();
				int numOfClusters = (Integer) numOfClustersValue.getValue();
				double deviation = (Double) standDeviationValue.getValue() / 1000;

				inputTextArea.setText("");

				Random rand = new Random();
				// go through each cluster
				for (int i = 0; i < numOfClusters; i++) {
					double meanX = (Map.bottomRigthMapGPS.x - Map.topLeftMap.x) * rand.nextDouble() + Map.topLeftMap.x;
					double meanY = (Map.bottomRigthMapGPS.y - Map.topLeftMap.y) * rand.nextDouble() + Map.topLeftMap.y;
					int leftOver = 0; // in case the number of points dont divide evenly into the number cluster
					if (i == numOfClusters - 1) {
						leftOver = maxPoints - numOfClusters * (maxPoints / numOfClusters);
					}
					// go through each point inside cluster i
					for (int j = 0; j < maxPoints / numOfClusters + leftOver; j++) {
						// generates x and y with normal(Gaussian) distribution
						double x = (rand.nextGaussian() * deviation + meanX);
						double y = (rand.nextGaussian() * deviation + meanY);

						// generates the time with a random distribution between 0-30
						int time = rand.nextInt(31);

						// the while statements make sure the points stay on the map
						// while preserving there distribution
						// to far to the right
						while (x > Map.bottomRigthMapGPS.x) {
							x -= (Map.bottomRigthMapGPS.x - Map.topLeftMap.x);
						}
						// to far to the down
						while (y < Map.bottomRigthMapGPS.y) {
							y += (Map.topLeftMap.y - Map.bottomRigthMapGPS.y);
						}
						// to far left
						while (x < Map.topLeftMap.x) {
							x += (Map.bottomRigthMapGPS.x - Map.topLeftMap.x);
						}
						// to far down
						while (y > Map.topLeftMap.y) {
							y -= (Map.topLeftMap.y - Map.bottomRigthMapGPS.y);
						}

						// types the results in the input box
						inputTextArea.setText(inputTextArea.getText() + "" + (j + i * (maxPoints / numOfClusters) + 1)
								+ ",This is a test address ," + time + "," + y + "," + x + "\n");
					}
				}

				// Triggers the submit button
				ctrlPanel.submitButtonAction();
			}
		});

		generatorFrame.getContentPane().add(comparePanel);
		generatorFrame.validate();
	}

	// returns the nearest customer to the specified coordinate
	public static int getCustomerClicked(Point point) {
		int nearestCustomerIndex = 0;
		double nearestDistance = 999999999;
		for (int i = 0; i < TSP.customers.length; i++) {
			Customer customer = TSP.customers[i];
			double distance = point.distance(Map.scalePoint(customer.location));
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearestCustomerIndex = i;
			}
		}
		return nearestCustomerIndex;
	}
}