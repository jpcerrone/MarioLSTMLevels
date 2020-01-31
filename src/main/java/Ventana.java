import engine.core.MarioSprite;
import engine.core.MarioWorld;
import engine.graphics.MarioImage;
import engine.helper.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class Ventana extends JFrame {
    JPanel panel;
    Ventana(){
        panel = new JPanel();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width,screenSize.height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        Assets.init(getGraphicsConfiguration());
        //panel.getGraphics().create();
    }

    public void dibujar(int x, int y,Image img){


        panel.getGraphics().drawImage(img, x, y, 16, 16, null);
    }

    public void dibujarEnemigo(int x, int y,Image img){
        panel.getGraphics().drawImage(img, x, y, 16, 32, null);
    }
}
