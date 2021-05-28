import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Gui {
	public static JTextArea inputTextArea;
	public static Map map;
	public static ControlPanel ctrlPanel;
	public static JList<String> algCompare;

	private final int ctrlWidth = 180;
	private final int inputBoxHeight = 200;

	private static final Dimension windowSize = new Dimension(1200, 1300);

	public Gui() {
		JFrame frame = new JFrame("Draw Graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setSize(windowSize);
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
				if (!Map.bestPathContains(customerID)) {
					int[] newBestPath = new int[Main.bestPath.length + 1];
					for (int i = 0; i < Main.bestPath.length; i++) {
						newBestPath[i] = Main.bestPath[i];
					}
					newBestPath[Main.bestPath.length] = customerID;
					Main.bestPath = newBestPath;
				} else {
					int[] newBestPath = new int[Main.bestPath.length - 1];
					for (int i = 0; i < newBestPath.length; i++) {
						if (i < customerID) {
							newBestPath[i] = Main.bestPath[i];
						} else {
							newBestPath[i] = Main.bestPath[i - 1];
						}
					}
					Main.bestPath = newBestPath;
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

	public static void startComparisionWindow() {
		JFrame frame = new JFrame("Algorithms Comparision");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setSize(1100, 300);
		frame.setVisible(true);

		JPanel comparePanel = new JPanel(new BorderLayout());

		String[] results = Algorithms.compareAlogrithemsWithResults();

		algCompare = new JList<String>(results); // data has type Object[]
		algCompare.setLayoutOrientation(JList.VERTICAL);
		algCompare.setVisibleRowCount(-1);
		algCompare.setFont(new Font("monospaced", Font.BOLD, 16));

		algCompare.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				System.out.println(e.getFirstIndex());
			}

		});

		JScrollPane listScroller = new JScrollPane(algCompare);
		listScroller.setPreferredSize(new Dimension(250, 80));
		comparePanel.add(listScroller, BorderLayout.CENTER);

		// TODO:click list draws on map

		JButton close = new JButton("Close");
		comparePanel.add(close, BorderLayout.SOUTH);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});

		frame.getContentPane().add(comparePanel);
		frame.validate();
	}
}