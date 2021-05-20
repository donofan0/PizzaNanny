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

	public ControlPanel(Rectangle frame) {
		this.setBounds(frame);
		this.setSize(frame.getSize());
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

		String[] algorithems = { "Nearest Neighbor", "Convex Hull", "Branch and Bound", "Two Opt Inversion" };
		JComboBox algorithmSelect = new JComboBox(algorithems);
		algorithmSelect.setPreferredSize(new Dimension(20, 20));
		c.gridy = 0;
		this.add(algorithmSelect, c);

		JProgressBar progress = new JProgressBar();
		c.gridy = 1;
		this.add(progress, c);

		JLabel punishment = new JLabel("Minues over 30: ");
		c.gridy = 2;
		this.add(punishment, c);

		JTextArea outputTextArea = new JTextArea();
		outputTextArea.setLineWrap(true);
		JScrollPane outputDeliverys = new JScrollPane(outputTextArea);
		outputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// outputDeliverys.setPreferredSize(new Dimension(100, 100));
		c.weighty = 40;
		c.gridy = 3;
		this.add(outputDeliverys, c);

		JButton address = new JButton("Show Address");
		c.weighty = 1;
		c.gridy = 4;
		this.add(address, c);

		JButton submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100, 20));
		c.gridy = 5;
		this.add(submit, c);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] input = Gui.inputTextArea.getText().split("\\n");
				Main.customers.clear();
				Main.bestPath.clear();
				for (int x = 0; x < input.length; x++) {
					String[] currentLine = input[x].split(",");
					Customer customer = new Customer(Integer.parseInt(currentLine[0].strip()), currentLine[1],
							Integer.parseInt(currentLine[2].strip()), Double.parseDouble(currentLine[3].strip()),
							Double.parseDouble(currentLine[4].strip()));
					Main.customers.add(customer);
				}
				Collections.sort(Main.customers);

				Gui.frame.getContentPane().remove(Gui.map);
				Gui.map = new Map(new Rectangle(0, 0, Gui.frame.getWidth() - 200, Gui.frame.getHeight() - 160));
				Gui.frame.getContentPane().add(Gui.map);
				Gui.frame.validate();

				int algSelected = algorithmSelect.getSelectedIndex();
				switch (algSelected) {
				case 0:
					Algorithms.calculateNearestNeighbor();
					break;
				case 1:
					Algorithms.calculateConvexHull();
					break;
				default:
					Algorithms.calculateNearestNeighbor();
					break;
				}

				String outputResult = "";
				for (int i = 0; i < Main.bestPath.size(); i++) {
					outputResult += Main.customers.get(Main.bestPath.get(i)).id + ", ";
				}
				outputTextArea.setText(outputResult);

				punishment.setText("Minues over 30: " + Algorithms.calculateMinutesOver(Main.bestPath));

				Gui.frame.getContentPane().remove(Gui.map);
				Gui.map = new Map(new Rectangle(0, 0, Gui.frame.getWidth() - 200, Gui.frame.getHeight() - 160));
				Gui.frame.getContentPane().add(Gui.map);
				Gui.frame.validate();
			}
		});
	}
}
