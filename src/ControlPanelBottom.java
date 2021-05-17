import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ControlPanelBottom extends JPanel {

	public ControlPanelBottom() {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		JTextArea inputTextArea = new JTextArea();
		JScrollPane inputDeliverys = new JScrollPane(inputTextArea);
		inputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		inputDeliverys.setPreferredSize(new Dimension(100, 100));
		this.add(inputDeliverys);

		JButton submit = new JButton("Submit");
		submit.setPreferredSize(new Dimension(100, 20));
		this.add(submit);
		submit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String[] input = inputTextArea.getText().split("\\n");
				for (int x = 0; x < input.length; x++) {
					String[] currentLine = input[x].split(",");
					Customer customer = new Customer(currentLine[1], Integer.parseInt(currentLine[2]),
							Double.parseDouble(currentLine[3]), Double.parseDouble(currentLine[4]));
					Main.customers.add(customer);
				}

				Gui.frame.getContentPane().remove(Gui.map);

				Gui.map = new Map(new Rectangle(0, 0, Gui.frame.getWidth(), Gui.frame.getHeight()));
				Gui.frame.getContentPane().add(Gui.map);
				Gui.frame.validate();
			}
		});
	}

}
