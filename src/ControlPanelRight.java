import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ControlPanelRight extends JPanel {

	public ControlPanelRight() {
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		String[] algorithems = { "Nearest Neighbor", "Brute Force", "Convex Hull", "Nerual Network" };
		JComboBox algorithmSelect = new JComboBox(algorithems);
		// algorithmSelect.setPreferredSize(new Dimension(20, 20));
		this.add(algorithmSelect);

		JProgressBar progress = new JProgressBar();
		this.add(progress);

		JButton address = new JButton("Show Address");
		this.add(address);

		JTextArea outputTextArea = new JTextArea();
		JScrollPane outputDeliverys = new JScrollPane(outputTextArea);
		outputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputDeliverys.setPreferredSize(new Dimension(100, 100));
		this.add(outputDeliverys);
	}
}
