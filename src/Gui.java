import java.awt.BorderLayout;
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
		JPanel linesPanel = new JPanel();
		JPanel rightPanel = new JPanel();

		pointsPanel.setOpaque(false);
		// pointsPanel.setBackground(new Color(0, 0, 0, 65));

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
		picLabel.setBounds(0, 0, 400, 400);
		mapPanel.add(picLabel, 0);

		pointsPanel.add(new Map());

		pointsPanel.setBounds(0, 0, 400, 400);
		mapPanel.setBounds(0, 0, 400, 400);

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new Map());
		frame.validate();
	}
}