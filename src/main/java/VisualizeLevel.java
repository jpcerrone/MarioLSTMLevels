
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.helper.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.awt.event.KeyEvent.*;

public class VisualizeLevel{




    public static void main(String[] args) throws IOException {
        Ventana ventana = new Ventana();
        ventana.setVisible(true);

    }

}
