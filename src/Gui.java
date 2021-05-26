import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Gui {
	public static JTextArea inputTextArea;
	public static Map map;

	private ControlPanel ctrlPanel;

	private final int ctrlWidth = 180;
	private final int inputBoxHeight = 200;

	// TODO: Add stats Page

	public Gui() {
		JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(1200, 1300);
		frame.setVisible(true);

		Dimension frameSize = frame.getContentPane().getSize();

		map = new Map(new Rectangle(0, 0, frameSize.width - ctrlWidth, frameSize.height - inputBoxHeight));
		ctrlPanel = new ControlPanel(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));

		inputTextArea = new JTextArea();
		JScrollPane inputDeliverys = new JScrollPane(inputTextArea);
		inputDeliverys.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		inputDeliverys.setBounds(
				new Rectangle(0, frameSize.height - inputBoxHeight, frameSize.width - ctrlWidth, inputBoxHeight));

		map.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!ctrlPanel.editMode) {
					return;
				}
				int customerID = Map.getCustomerClicked(e.getPoint());
				if (!Main.bestPath.contains(customerID)) {
					Main.bestPath.add(customerID);
				} else {
					Main.bestPath.removeAll(Collections.singleton(customerID));
				}

				ctrlPanel.drawOutput();

				map.drawPoints();
				map.drawLines();
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});

		frame.getContentPane().add(ctrlPanel);
		frame.getContentPane().add(inputDeliverys);
		frame.getContentPane().add(map);

		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				Dimension frameSize = frame.getContentPane().getSize();

				ctrlPanel.setBounds(new Rectangle(frameSize.width - ctrlWidth, 0, ctrlWidth, frameSize.height));
				inputDeliverys.setBounds(new Rectangle(0, frameSize.height - inputBoxHeight,
						frameSize.width - ctrlWidth, inputBoxHeight));

				Rectangle mapDemensions = new Rectangle(0, 0, frameSize.width - ctrlWidth,
						frameSize.height - inputBoxHeight);
				map.reSizeMap(mapDemensions);

				frame.validate();
			}
		});

		frame.validate();
	}
}