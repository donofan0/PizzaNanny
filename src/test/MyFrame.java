package test;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class MyFrame extends JFrame{

    //main method called on startup
    public static void main(String[] args) throws InterruptedException {
    
        //creates a frame window
        MyFrame frame = new MyFrame();
    
        //very basic game loop where the graphics are re-rendered
        while(true){
            frame.getPanel().repaint();
        
            //The program waits a while before rerendering
            Thread.sleep(12);
        }
    }

    //the MyPanel is the other class and it extends JPanel
    private MyPanel panel;

    //constructor that sets some basic staring values
    public MyFrame(){
        this.setSize(500, 500);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        //creates the MyPanel with paramaters of x=0 and y=0
        panel = new MyPanel(0,0);
        //adds the panel (which is a JComponent because it extends JPanel) 
        //into the frame 
        this.add(panel);
        //shows the frame window
        this.setVisible(true);
    }
 
    //gets the panel 
    public MyPanel getPanel(){
        return panel;
    }

}


class MyPanel extends JPanel{

    //two int variables to store the x and y coordinate 
    private int x;
    private int y;

    //construcor of the MyPanel class
    public MyPanel(int x, int y){
        this.x = x;
        this.y = y;
    }

    /*the method that deals with the graphics 
        this method is called when the component is first loaded, 
         when the component is resized and when the repaint() method is 
        called for this component
    */
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        //changes the x and y varible values
        x++;
        y++;

        //draws a rectangle at the x and y values
        g.fillRect(x, y, 50, 50);
    }

}