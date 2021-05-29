import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
	public static JTextArea inputTextArea;
	public static Map map;
	public static ControlPanel ctrlPanel;
	public static JList<String> algCompare;

	private final int ctrlWidth = 180;
	private final int inputBoxHeight = 200;

	private static final Dimension windowSize = new Dimension(1200, 1300);
	private static JFrame generatorFrame;
	private static JFrame compareFrame;

	public Gui() {
		JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(windowSize);
		frame.setVisible(true);

		Dimension frameSize = frame.getContentPane().getSize();

		map = new Map(new Rectangle(0, 0, frameSize.width - ctrlWidth, frameSize.height - inputBoxHeight));
		ctrlPanel = new ControlPanel(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));

		inputTextArea = new JTextArea();
		JScrollPane inputDeliverys = new JScrollPane(inputTextArea);
		inputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		inputDeliverys.setBounds(
				new Rectangle(0, frameSize.height - inputBoxHeight, frameSize.width - ctrlWidth, inputBoxHeight));

		map.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!ctrlPanel.editMode) {
					return;
				}
				int customerID = Map.getCustomerClicked(e.getPoint());
				if (!Map.bestPathContains(customerID)) {
					int[] newBestPath = new int[Main.bestPath.length + 1];
					for (int i = 0; i < Main.bestPath.length; i++) {
						newBestPath[i] = Main.bestPath[i];
					}
					newBestPath[Main.bestPath.length] = customerID;
					Main.bestPath = newBestPath;
				} else {
					Map.bestPathRemove(customerID);
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

		frame.getContentPane().add(ctrlPanel);
		frame.getContentPane().add(inputDeliverys);
		frame.getContentPane().add(map);

		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				Dimension frameSize = frame.getContentPane().getSize();

				ctrlPanel.setBounds(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));
				inputDeliverys.setBounds(new Rectangle(0, frameSize.height - inputBoxHeight,
						frameSize.width - ctrlWidth, inputBoxHeight));

				Rectangle mapDemensions = new Rectangle(0, 0, frameSize.width - ctrlWidth,
						frameSize.height - inputBoxHeight);
				map.reSizeMap(mapDemensions);

				frame.validate();
			}
		});

		frame.validate();
	}

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

		String[] results = Algorithms.compareAlogrithemsWithResults();

		algCompare = new JList<String>(results); // data has type Object[]
		algCompare.setLayoutOrientation(JList.VERTICAL);
		algCompare.setFont(new Font("monospaced", Font.BOLD, 16));
		algCompare.setSelectedIndex(0);

		algCompare.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int selectedAlg = algCompare.getSelectedIndex() - 1;
				if (selectedAlg < 0 || selectedAlg > Algorithms.algorithms.length - 1) {
					return;
				}

				System.out.println(selectedAlg);

				ctrlPanel.algorithmSelect.setSelectedIndex(selectedAlg);
				ctrlPanel.submitButtonAction();
			}

		});

		JScrollPane listScroller = new JScrollPane(algCompare);
		listScroller.setPreferredSize(new Dimension(250, 80));
		comparePanel.add(listScroller, BorderLayout.CENTER);

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
		JSpinner numOfCustomersValue = new JSpinner(spinModel);
		c.gridx = 1;
		comparePanel.add(numOfCustomersValue, c);

		Label = new JLabel("Number of Clusters:");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(Label, c);

		spinModel = new SpinnerNumberModel(5, 0, 99, 1);
		JSpinner numOfClustersValue = new JSpinner(spinModel);
		c.gridx = 1;
		comparePanel.add(numOfClustersValue, c);

		Label = new JLabel("Standard Deviation:");
		c.gridx = 0;
		c.gridy++;
		comparePanel.add(Label, c);

		spinModel = new SpinnerNumberModel(1, 0, 100, 0.5);
		JSpinner standDeviationValue = new JSpinner(spinModel);
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
				int maxPoints = (int) numOfCustomersValue.getValue();
				int numOfClusters = (int) numOfClustersValue.getValue();
				double deviation = (double) standDeviationValue.getValue() / 1000;

				inputTextArea.setText("");

				Random rand = new Random();
				for (int i = 0; i < numOfClusters; i++) {
					double meanX = (Map.bottomRigthMapGPS.x - Map.topLeftMap.x) * rand.nextDouble() + Map.topLeftMap.x;
					double meanY = (Map.bottomRigthMapGPS.y - Map.topLeftMap.y) * rand.nextDouble() + Map.topLeftMap.y;
					int leftOver = 0;
					if (i == numOfClusters - 1) {
						leftOver = maxPoints - numOfClusters * (maxPoints / numOfClusters);
					}
					for (int j = 0; j < maxPoints / numOfClusters + leftOver; j++) {
						double x = (rand.nextGaussian() * deviation + meanX);
						double y = (rand.nextGaussian() * deviation + meanY);
						int time = rand.nextInt(31);

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

						inputTextArea.setText(inputTextArea.getText() + "" + (j + i * (maxPoints / numOfClusters) + 1)
								+ ",This is a test address ," + time + "," + y + "," + x + "\n");
					}
				}

				ctrlPanel.submitButtonAction();
			}
		});

		generatorFrame.getContentPane().add(comparePanel);
		generatorFrame.validate();
	}
}