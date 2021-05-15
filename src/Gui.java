import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Gui {
	public Gui() {
		JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setSize(1000, 800);
		frame.setVisible(true);

		JPanel masterPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		JPanel centrePanel = new JPanel();
		JPanel bottomPanel = new JPanel();

		frame.getContentPane().add(new Map());

		masterPanel.add(topPanel, BorderLayout.PAGE_START);
		masterPanel.add(leftPanel, BorderLayout.LINE_START);
		masterPanel.add(rightPanel, BorderLayout.LINE_END);
		masterPanel.add(centrePanel, BorderLayout.CENTER);
		masterPanel.add(bottomPanel, BorderLayout.PAGE_END);
	}
}