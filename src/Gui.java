import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
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
		JLayeredPane leftPanel = new JLayeredPane();
		JPanel rightPanel = new JPanel();
		JPanel centrePanel = new JPanel();
		JPanel bottomPanel = new JPanel();

		masterPanel.add(topPanel, BorderLayout.PAGE_START);
		masterPanel.add(leftPanel, BorderLayout.LINE_START);
		masterPanel.add(rightPanel, BorderLayout.LINE_END);
		masterPanel.add(centrePanel, BorderLayout.CENTER);
		masterPanel.add(bottomPanel, BorderLayout.PAGE_END);

		Dimension layeredPaneSize = new Dimension(300, 310);
		// leftPanel.setPreferredSize(layeredPaneSize);
		BufferedImage myPicture = null;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		picLabel.setBounds(0, 0, 300, 300);
		leftPanel.add(picLabel, 0);
		// leftPanel.add(new Map());
		frame.getContentPane().add(leftPanel);
		frame.validate();
	}
}