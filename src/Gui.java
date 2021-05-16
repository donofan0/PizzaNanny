import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

public class Gui {
	private JFrame frame;
	private Map map;

	public Gui() {
		frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setSize(1071, 777);
		frame.setVisible(true);
		// 1.23672 : 1 is the aspect ratio of the map image
		map = new Map(new Rectangle(0, 0, frame.getWidth(), frame.getHeight()));

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(map);

		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				frame.getContentPane().remove(map);

				map = new Map(new Rectangle(0, 0, frame.getWidth(), frame.getHeight()));
				frame.getContentPane().add(map);
				frame.validate();
			}
		});
		frame.validate();
	}
}