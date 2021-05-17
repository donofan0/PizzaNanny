import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;

public class Gui {
	public static JFrame frame;
	public static Map map;
	private ControlPanelRight controlPanelRight;
	private ControlPanelBottom controlPanelBottom;

	public Gui() {
		frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setSize(1071, 777);
		frame.setVisible(true);

		map = new Map(new Rectangle(0, 0, frame.getWidth() - 100, frame.getHeight() - 100));
		controlPanelRight = new ControlPanelRight();
		controlPanelBottom = new ControlPanelBottom();

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(map);
		frame.getContentPane().add(controlPanelRight, BorderLayout.EAST);
		frame.getContentPane().add(controlPanelBottom, BorderLayout.SOUTH);

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