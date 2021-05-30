import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

public class ControlPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8430168436937097596L;

	public static boolean showEmoji = false;
	public static int repeatSteps = 10;
	public static boolean droneRunning = false;
	public static boolean groupAlgorithmRunning = false;
	public static boolean branchAlgorithmRunning = false;
	public static JButton startDrone;
	public static JProgressBar progress;
	public static JLabel bestAlgorithem;

	public boolean editMode = false;
	public JComboBox algorithmSelect;

	private boolean showAddress = false;
	private JTextArea outputTextArea;
	private JLabel punishment;
	private JLabel distance;
	private JSpinner repeatStepsValue;
	private JButton editModeToggle;
	private JButton emojiToggle;
	private JButton addressToggle;

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
		c.insets = new Insets(4, 4, 4, 4);
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;

		// used to select the chosen algorithm
		algorithmSelect = new JComboBox(Algorithms.algorithms);
		algorithmSelect.setPreferredSize(new Dimension(20, 20));
		algorithmSelect.setSelectedIndex(Algorithms.algorithms.length - 1);
		this.add(algorithmSelect, c);

		// progress bar
		progress = new JProgressBar(0, 100);
		c.gridy++;
		this.add(progress, c);

		// repeat steps identifies how many times to rework the path which was output
		// from the chosen algorithm
		JLabel repeatStepsLabel = new JLabel("Repeat Steps:");
		c.gridwidth = 1;
		c.gridy++;
		this.add(repeatStepsLabel, c);

		SpinnerNumberModel spinModel = new SpinnerNumberModel(10, 0, 10, 1);
		repeatStepsValue = new JSpinner(spinModel);
		c.gridx = 1;
		this.add(repeatStepsValue, c);

		// displays angry Minutes
		punishment = new JLabel("Angry Minutes: ");
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy++;
		this.add(punishment, c);

		// displays total distance
		distance = new JLabel("Total distance: m");
		c.gridy++;
		this.add(distance, c);

		// displays the best algorithm when using all of the above algorithm
		bestAlgorithem = new JLabel("");
		c.gridy++;
		this.add(bestAlgorithem, c);

		// where the output list is displayed
		outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		JScrollPane outputDeliverys = new JScrollPane(outputTextArea);
		outputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		c.weighty = 90;
		c.gridy++;
		this.add(outputDeliverys, c);

		// toggles whether to display the addresses
		addressToggle = new JButton("Show Address");
		c.weighty = 1;
		c.gridy++;
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

				drawOutput();
			}
		});

		// toggle whether to display emoji instead of dots
		emojiToggle = new JButton("Show Emoji");
		c.gridy++;
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
			}
		});

		// button to start the drone
		startDrone = new JButton("Start Drone");
		c.gridy++;
		this.add(startDrone, c);
		startDrone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (droneRunning) {
					droneRunning = false;
					startDrone.setText("Start Drone");
				} else {
					droneRunning = true;
					startDrone.setText("Stop Drone");

					Gui.map.startDrone();
				}
			}
		});

		// clears the path and starts edit mode
		editModeToggle = new JButton("Start Edit Mode");
		c.gridy++;
		this.add(editModeToggle, c);
		editModeToggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (editMode) {
					editMode = false;
					editModeToggle.setText("Start Edit Mode");
				} else {
					editMode = true;
					editModeToggle.setText("Stop Edit Mode");

					TSP.bestPath = new int[0];
					Gui.map.drawLines();
					Gui.map.drawPoints();
				}
			}
		});

		// opens the points generator screen
		JButton openRandomPoints = new JButton("Generate Points");
		c.gridy++;
		this.add(openRandomPoints, c);
		openRandomPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Gui.startPointsGenerator();
			}
		});

		// opens the points Comparison screen
		JButton openComparison = new JButton("Open Comparison");
		c.gridy++;
		this.add(openComparison, c);
		openComparison.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getUserInput();
				if (TSP.customers.length > 0) {
					Gui.startComparisionWindow();
				}
			}
		});

		// submits the data in the input box and runs the algorithm
		// also resets and cancels a lot of actions
		JButton submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100, 20));
		c.gridy++;
		this.add(submit, c);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				submitButtonAction();
			}
		});
	}

	// inputs the data in the input box into the system and runs the chosen
	// algorithm
	// also resets and cancels a lot of actions
	public void submitButtonAction() {
		// brings the user input into the customers array
		getUserInput();

		// draw the points
		Gui.map.drawPoints();

		// runs the algorithem which is selected
		int algSelected = algorithmSelect.getSelectedIndex();
		switch (algSelected) {
		case 0:
			Algorithms.calculateNearestNeighbor(false);
			break;
		case 1:
			Algorithms.calculateNearestNeighbor(true);
			break;
		case 2:
			Algorithms.calculateConvexHull(false);
			break;
		case 3:
			Algorithms.calculateConvexHull(true);
			break;
		case 4:
			Algorithms.calculateLargestTimeFirst();
			break;
		case 5:
			Algorithms.calculateGroupAproximition(null);
			break;
		case 6:
			Algorithms.calculateBranchAndBound();
			break;
		default:
			int best = Algorithms.compareAlogrithems();
			bestAlgorithem.setText("Best: " + algorithmSelect.getItemAt(best));
			break;
		}
		// does not draw the output if it is one of the asynchronous algorithms
		if (algSelected != 6 || algSelected != 5) {
			// draws the output and lines
			drawOutput();
			Gui.map.drawLines();
		}
	}

	// brings the user input into the customers array
	// as well as cancels and resets some tasks
	public void getUserInput() {
		// stops the asynchronous algorithms if they are running
		groupAlgorithmRunning = false;
		branchAlgorithmRunning = false;

		// brings the user input into the customers array
		// array list is used because at this point i dont know how many customers there
		// are
		// this is because the code is tolerant to empty lines
		String[] input = Gui.inputTextArea.getText().trim().split("\\n");
		ArrayList<Customer> tempCustomers = new ArrayList<Customer>();
		for (int x = 0; x < input.length; x++) {
			String[] currentLine = input[x].split(",");
			if (currentLine.length < 5) {
				continue;
			}
			Customer customer = new Customer(Integer.parseInt(currentLine[0].trim()), currentLine[1],
					Integer.parseInt(currentLine[2].trim()), Double.parseDouble(currentLine[3].trim()),
					Double.parseDouble(currentLine[4].trim()));
			tempCustomers.add(customer);
		}
		// convert from arraylist to int[]
		TSP.customers = new Customer[tempCustomers.size()];
		TSP.customers = tempCustomers.toArray(TSP.customers);

		if (TSP.customers.length < 1) {
			return;
		}

		repeatSteps = Integer.parseInt(repeatStepsValue.getValue().toString());

		// stops the drone and edit mode if running
		droneRunning = false;
		startDrone.setText("Start Drone");
		editMode = false;
		editModeToggle.setText("Start Edit Mode");
		bestAlgorithem.setText("");
	}

	// draws all the statistics and the outputed list
	public void drawOutput() {
		if (TSP.bestPath.length < 1) {
			return;
		}

		// used to add commas and round numbers
		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setMaximumFractionDigits(0);
		numFormat.setMinimumIntegerDigits(2);

		// displays both the angry minites and the total distance
		double[] timeDistance = Algorithms.calculateTimeDistance();
		punishment.setText("Angry Minutes: " + numFormat.format(timeDistance[0]));
		distance.setText("Total distance: " + numFormat.format(timeDistance[1]) + "m");

		// displays the output list for copy pasting
		String outputResult = "";
		for (int i = 0; i < TSP.bestPath.length - 1; i++) {
			outputResult += TSP.customers[TSP.bestPath[i]].id;
			if (showAddress) {
				outputResult += "(" + TSP.customers[TSP.bestPath[i]].address + ")";
			}
			outputResult += ",";
		}
		// last item in the list is seperate so it doesent have a comma at the end
		outputResult += TSP.customers[TSP.bestPath[TSP.bestPath.length - 1]].id;
		if (showAddress) {
			outputResult += "(" + TSP.customers[TSP.bestPath[TSP.bestPath.length - 1]].address + ")";
		}
		outputTextArea.setText(outputResult);
	}
}
