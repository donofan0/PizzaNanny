import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

		masterPanel.add(topPanel, BorderLayout.PAGE_START);
		masterPanel.add(leftPanel, BorderLayout.LINE_START);
		masterPanel.add(rightPanel, BorderLayout.LINE_END);
		masterPanel.add(centrePanel, BorderLayout.CENTER);
		masterPanel.add(bottomPanel, BorderLayout.PAGE_END);

		BufferedImage myPicture = null;
		try {
			myPicture = ImageIO.read(new File("map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		leftPanel.add(picLabel);

		// frame.getContentPane().add(new Map());
		frame.getContentPane().add(masterPanel);

	}
}