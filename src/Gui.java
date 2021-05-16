import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

public class Gui {
	public Gui() {
		JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setSize(1000, 800);
		frame.setVisible(true);
		// 1.23672 : 1 is the aspect ratio of the map image
		JLayeredPane map = new Map((int) Math.round(frame.getHeight() * 1.23672), frame.getHeight());

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(map);
		frame.validate();
	}
}