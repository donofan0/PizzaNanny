import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ControlPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8430168436937097596L;

	final static int convexHullRepeatSteps = 20;

	static boolean showEmoji = false;
	static boolean droneRunning = false;

	boolean editMode = false;
	boolean showAddress = false;

	public ControlPanel(Rectangle frame) {
		this.setBounds(frame);
		this.setSize(frame.getSize());
		this.setBounds(frame);
		this.setPreferredSize(frame.getSize());
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// Default constants
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(10, 10, 10, 10);
		c.weighty = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;

		String[] algorithems = { "Nearest Neighbor", "Largest Time", "Convex Hull(Distance)", "Convex Hull(Time)",
				"Branch and Bound", "Two Opt Inversion" };
		JComboBox<String> algorithmSelect = new JComboBox<String>(algorithems);
		algorithmSelect.setPreferredSize(new Dimension(20, 20));
		c.gridy = 0;
		this.add(algorithmSelect, c);

		JProgressBar progress = new JProgressBar();
		c.gridy = 1;
		this.add(progress, c);

		JLabel punishment = new JLabel("Angry Minutes: ");
		c.gridy = 2;
		this.add(punishment, c);

		JLabel distance = new JLabel("Total distance: m");
		c.gridy = 3;
		this.add(distance, c);

		JTextArea outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		JScrollPane outputDeliverys = new JScrollPane(outputTextArea);
		outputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// outputDeliverys.setPreferredSize(new Dimension(100, 100));
		c.weighty = 40;
		c.gridy = 4;
		this.add(outputDeliverys, c);

		JButton addressToggle = new JButton("Show Address");
		c.weighty = 1;
		c.gridy = 5;
		this.add(addressToggle, c);
		addressToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showAddress) {
					showAddress = false;
					addressToggle.setText("Show Address");
				} else {
					showAddress = true;
					addressToggle.setText("Hide Address");
				}

				drawOutput(outputTextArea);
			}
		});

		JButton emojiToggle = new JButton("Show Emoji");
		c.gridy = 6;
		this.add(emojiToggle, c);
		emojiToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (showEmoji) {
					showEmoji = false;
					emojiToggle.setText("Show Emoji");
				} else {
					showEmoji = true;
					emojiToggle.setText("Hide Emoji");
				}

				Gui.map.drawPoints();

				Gui.frame.validate();
			}
		});

		JButton editModeToggle = new JButton("Start Edit Mode");
		c.gridy = 7;
		this.add(editModeToggle, c);
		editModeToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (editMode) {
					editMode = false;
					editModeToggle.setText("Start Edit Mode");
				} else {
					editMode = true;
					editModeToggle.setText("Stop Edit Mode");

					Main.bestPath.clear();
					Gui.map.drawLines();
					Gui.map.drawPoints();
				}
			}
		});

		JButton startDrone = new JButton("Start Drone");
		c.gridy = 8;
		this.add(startDrone, c);
		startDrone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (droneRunning) {
					droneRunning = false;
					startDrone.setText("Start Drone");
				} else {
					droneRunning = true;
					startDrone.setText("stop Drone");

					Gui.map.startDrone();

					Gui.frame.validate();
				}
			}
		});

		// TODO: generate random points

		JButton submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100, 20));
		c.gridy = 9;
		this.add(submit, c);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] input = Gui.inputTextArea.getText().trim().split("\\n");
				Main.customers.clear();
				Main.bestPath.clear();
				for (int x = 0; x < input.length; x++) {
					String[] currentLine = input[x].split(",");
					if (currentLine.length < 5) {
						continue;
					}
					Customer customer = new Customer(Integer.parseInt(currentLine[0].strip()), currentLine[1],
							Integer.parseInt(currentLine[2].strip()), Double.parseDouble(currentLine[3].strip()),
							Double.parseDouble(currentLine[4].strip()));
					Main.customers.add(customer);
				}
				Collections.sort(Main.customers);

				droneRunning = false;
				startDrone.setText("Start Drone");

				Gui.map.drawPoints();

				int algSelected = algorithmSelect.getSelectedIndex();
				switch (algSelected) {
				case 0:
					Algorithms.calculateNearestNeighbor();
					break;
				case 1:
					Algorithms.calculateLargestTimeFirst();
					break;
				case 2:
					Algorithms.calculateConvexHull(false, 0);
					break;
				case 3:
					Algorithms.calculateConvexHull(true, convexHullRepeatSteps);
					break;
				default:
					Algorithms.calculateNearestNeighbor();
					break;
				}

				drawOutput(outputTextArea);

				double[] timeDistance = Map.calculateTimeDistance(Main.bestPath);
				punishment.setText("Angry Minutes: " + Math.round(timeDistance[0]));
				distance.setText("Total distance: " + Math.round(timeDistance[1]) + "m");

				Gui.map.drawLines();

				Gui.frame.validate();
			}
		});
	}

	public void drawOutput(JTextArea outputTextArea) {
		String outputResult = "";
		for (int i = 0; i < Main.bestPath.size() - 1; i++) {
			outputResult += Main.customers.get(Main.bestPath.get(i)).id;
			if (showAddress) {
				outputResult += "(" + Main.customers.get(Main.bestPath.get(i)).address + ")";
			}
			outputResult += ",";
		}

		outputResult += Main.customers.get(Main.bestPath.get(Main.bestPath.size() - 1)).id;
		if (showAddress) {
			outputResult += "(" + Main.customers.get(Main.bestPath.get(Main.bestPath.size() - 1)).address + ")";
		}

		outputTextArea.setText(outputResult);
	}
}
