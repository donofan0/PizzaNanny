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

	boolean editMode = false;
	boolean showAddress = false;
	JComboBox<String> algorithmSelect;

	private JTextArea outputTextArea;
	private JLabel punishment;
	private JLabel distance;
	private JSpinner repeatStepsValue;
	private JButton editModeToggle;
	private JLabel bestAlgorithem;
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

		algorithmSelect = new JComboBox<String>(Algorithms.algorithms);
		algorithmSelect.setPreferredSize(new Dimension(20, 20));
		algorithmSelect.setSelectedIndex(Algorithms.algorithms.length - 1);
		this.add(algorithmSelect, c);

		progress = new JProgressBar(0, 100);
		c.gridy++;
		this.add(progress, c);

		JLabel repeatStepsLabel = new JLabel("Repeat Steps:");
		c.gridwidth = 1;
		c.gridy++;
		this.add(repeatStepsLabel, c);

		SpinnerNumberModel spinModel = new SpinnerNumberModel(10, 0, 10, 1);
		repeatStepsValue = new JSpinner(spinModel);
		c.gridx = 1;
		this.add(repeatStepsValue, c);

		punishment = new JLabel("Angry Minutes: ");
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy++;
		this.add(punishment, c);

		distance = new JLabel("Total distance: m");
		c.gridy++;
		this.add(distance, c);

		bestAlgorithem = new JLabel("");
		c.gridy++;
		this.add(bestAlgorithem, c);

		outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		JScrollPane outputDeliverys = new JScrollPane(outputTextArea);
		outputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		c.weighty = 90;
		c.gridy++;
		this.add(outputDeliverys, c);

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

					Main.bestPath = new int[0];
					Gui.map.drawLines();
					Gui.map.drawPoints();
				}
			}
		});

		JButton openRandomPoints = new JButton("Generate Points");
		c.gridy++;
		this.add(openRandomPoints, c);
		openRandomPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Gui.startPointsGenerator();
			}
		});

		JButton openComparison = new JButton("Open Comparison");
		c.gridy++;
		this.add(openComparison, c);
		openComparison.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				submitButtonAction();
				if (Main.customers.length > 0) {
					Gui.startComparisionWindow();
				}
			}
		});

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

	public void submitButtonAction() {
		groupAlgorithmRunning = false;
		branchAlgorithmRunning = false;

		String[] input = Gui.inputTextArea.getText().trim().split("\\n");
		ArrayList<Customer> tempCustomers = new ArrayList<Customer>();
		for (int x = 0; x < input.length; x++) {
			String[] currentLine = input[x].split(",");
			if (currentLine.length < 5) {
				continue;
			}
			Customer customer = new Customer(Integer.parseInt(currentLine[0].strip()), currentLine[1],
					Integer.parseInt(currentLine[2].strip()), Double.parseDouble(currentLine[3].strip()),
					Double.parseDouble(currentLine[4].strip()));
			tempCustomers.add(customer);
		}
		Main.customers = new Customer[tempCustomers.size()];
		Main.customers = tempCustomers.toArray(Main.customers);

		repeatSteps = Integer.parseInt(repeatStepsValue.getValue().toString());

		droneRunning = false;
		startDrone.setText("Start Drone");
		editMode = false;
		editModeToggle.setText("Start Edit Mode");

		Gui.map.drawPoints();

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
			Algorithms.calculateGroupAproximition();
			break;
		case 6:
			Algorithms.calculateBranchAndBound();
			break;
		default:
			int best = Algorithms.compareAlogrithems();
			bestAlgorithem.setText("Best: " + algorithmSelect.getItemAt(best));
			break;
		}
		if (algSelected != 6) {
			drawOutput();
			Gui.map.drawLines();
		}
	}

	public void drawOutput() {
		if (Main.bestPath.length < 1) {
			return;
		}

		NumberFormat numFormat = NumberFormat.getInstance();
		numFormat.setMaximumFractionDigits(0);
		numFormat.setMinimumIntegerDigits(2);

		double[] timeDistance = Map.calculateTimeDistance();
		punishment.setText("Angry Minutes: " + numFormat.format(timeDistance[0]));
		distance.setText("Total distance: " + numFormat.format(timeDistance[1]) + "m");

		String outputResult = "";
		for (int i = 0; i < Main.bestPath.length - 1; i++) {
			outputResult += Main.customers[Main.bestPath[i]].id;
			if (showAddress) {
				outputResult += "(" + Main.customers[Main.bestPath[i]].address + ")";
			}
			outputResult += ",";
		}

		outputResult += Main.customers[Main.bestPath[Main.bestPath.length - 1]].id;
		if (showAddress) {
			outputResult += "(" + Main.customers[Main.bestPath[Main.bestPath.length - 1]].address + ")";
		}

		outputTextArea.setText(outputResult);
	}
}
