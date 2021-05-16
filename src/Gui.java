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

		JLayeredPane masterPanel = new JLayeredPane();
		JPanel mapPanel = new JPanel();
		JPanel pointsPanel = new JPanel();

		masterPanel.add(mapPanel, JLayeredPane.DEFAULT_LAYER);
		masterPanel.add(pointsPanel, JLayeredPane.PALETTE_LAYER);

		BufferedImage myPicture = null;
		try {
			// TODO: fix directory
			myPicture = ImageIO.read(new File("./src/map.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		mapPanel.setBounds(0, 0, 400, 400);
		mapPanel.add(picLabel, 0);

		Map map = new Map();
		map.setPreferredSize(new Dimension(400, 400));
		pointsPanel.setBounds(0, 0, 400, 400);
		pointsPanel.setOpaque(false);
		pointsPanel.add(map);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(masterPanel);
		frame.validate();
	}
}