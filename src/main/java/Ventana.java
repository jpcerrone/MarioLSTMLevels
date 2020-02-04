import engine.core.MarioSprite;
import engine.core.MarioWorld;
import engine.graphics.MarioImage;
import engine.helper.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

import static java.awt.event.KeyEvent.*;

public class Ventana extends JFrame implements KeyListener {
    private JPanel panel;
    private Dimension screenSize;
    private Image img;
    private int levelWidth;
    private int levelHeight;
    private int xShift;
    Ventana(){
        super("Level Visualizer");
        xShift = 0;
        panel = new Panel();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width,screenSize.height);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        Assets.init(getGraphicsConfiguration());
        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        //keyboard controls
        switch (e.getKeyCode()) {
            case VK_RIGHT:
                if(xShift < levelWidth-screenSize.width)
                    xShift += 16; break;
            case VK_LEFT:
                if(xShift > 0)
                     xShift -= 16; break;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class Panel extends JPanel{
        public void paintComponent(Graphics g){
            g.drawImage(img, 0 - xShift, screenSize.height/2 -16*16, levelWidth, levelHeight, null);
        }
    }

    public void dibujar(Image img, int width, int height){
        this.img = img;
        this.levelWidth = width;
        this.levelHeight = height;
        repaint();
    }

}
