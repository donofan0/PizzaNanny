import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Gui {
	public static JFrame frame;
	public static Map map;
	public static JTextArea inputTextArea;
	public static JPanel animation;
	private ControlPanel controlPanel;
	
	public Gui() {
		frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Display the window.
		frame.pack();
		frame.setSize(1200, 1300);
		frame.setVisible(true);

		map = new Map(new Rectangle(0, 0, frame.getWidth() - 200, frame.getHeight() - 160), false);
		controlPanel = new ControlPanel(new Rectangle(frame.getWidth() - 200, 0, 200, frame.getHeight()));

		inputTextArea = new JTextArea();
		JScrollPane inputDeliverys = new JScrollPane(inputTextArea);
		inputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		controlPanel.setBounds(new Rectangle(frame.getWidth() - 200, 0, 200, frame.getHeight() - 35));
		inputDeliverys.setBounds(new Rectangle(0, frame.getHeight() - 160, frame.getWidth() - 200, 125));

		frame.getContentPane().add(controlPanel);
		frame.getContentPane().add(inputDeliverys);
		frame.getContentPane().add(map);
		
		//sets up the animation glass Pane
        animation = new JPanel(null);
        frame.setGlassPane(animation);
        animation.setVisible(true);
        animation.setOpaque(false);

		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				controlPanel.setBounds(new Rectangle(frame.getWidth() - 200, 0, 200, frame.getHeight() - 35));
				inputDeliverys.setBounds(new Rectangle(0, frame.getHeight() - 160, frame.getWidth() - 200, 125));

				frame.getContentPane().remove(map);
				map = new Map(new Rectangle(0, 0, frame.getWidth() - 200, frame.getHeight() - 160), false);
				frame.getContentPane().add(map);
				frame.validate();
			}
		});

		frame.validate();
	}
}