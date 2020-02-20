import engine.core.MarioLevelModel;
import engine.core.MarioSprite;
import engine.core.MarioWorld;
import engine.graphics.MarioImage;
import engine.helper.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;

public class Ventana extends JFrame implements KeyListener {
    private JPanel panel;
    private Dimension screenSize;
    private Image img;
    private int levelWidth;
    private int levelHeight;
    private int xShift;

    private final String levelsFolderPath = "levels/jC";
    private int currentLevel;
    private Ventana ventana;
    private File[] levels;

    private static final int BLOCKSIZE = 16;
    private static final int FLAG_START = 4;
    private static final int FLAG_END = 14;

    Ventana() throws IOException {
        super("Level Visualizer");
        xShift = 0;
        panel = new Panel();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width,screenSize.height);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        Assets.init(getGraphicsConfiguration());
        File levelFolder = new File(levelsFolderPath);
        levels = levelFolder.listFiles();
        currentLevel = 0;

        loadLevel();
        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
    }
    private void loadLevel() throws IOException {
        File level = levels[currentLevel];
        List<String> lines = Files.readAllLines(level.toPath());
        int width = lines.get(0).length();
        BufferedImage img = new BufferedImage(width*BLOCKSIZE,BLOCKSIZE*BLOCKSIZE,BufferedImage.TYPE_INT_RGB);
        Graphics imgGraphics = img.getGraphics();
        List<Integer> flags = new ArrayList<>(); //List of all flag coordinates, given by the y component
        for(int j = 0 ; j < lines.size() ;j++){
            String line = lines.get(j);
            for(int i = 0; i < width;i++){
                Image sprite;
                boolean isEnemy = false;
                switch (line.charAt(i)) {
                    case MarioLevelModel.GROUND: {
                        sprite = Assets.level[1][0];
                        break;
                    }
                    case MarioLevelModel.EMPTY: {
                        sprite = Assets.level[6][4];
                        break;
                    }
                    case MarioLevelModel.NORMAL_BRICK: {
                        sprite = Assets.level[6][0];
                        break;
                    }
                    case MarioLevelModel.COIN: {
                        sprite = Assets.level[7][1];
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.PIPE:
                    case MarioLevelModel.PIPE_FLOWER: {
                        sprite = Assets.level[4][2];
                        break;
                    }
                    case MarioLevelModel.GOOMBA: {
                        sprite = Assets.enemies[0][2];
                        isEnemy = true;
                        break;
                    }
                    case MarioLevelModel.PYRAMID_BLOCK: {
                        sprite = Assets.level[2][0];
                        break;
                    }
                    case MarioLevelModel.GREEN_KOOPA: {
                        sprite = Assets.enemies[0][1];
                        isEnemy = true;
                        break;
                    }
                    case MarioLevelModel.RED_KOOPA: {
                        sprite = Assets.enemies[0][0];
                        isEnemy = true;
                        break;
                    }
                    case MarioLevelModel.SPECIAL_BRICK: {
                        sprite = Assets.items[0][0];
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.LIFE_BRICK: {
                        sprite = Assets.items[1][1];
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.COIN_BRICK: {
                        sprite = Assets.level[7][1];
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.COIN_QUESTION_BLOCK: {
                        sprite = Assets.level[3][1];
                        break;
                    }
                    case MarioLevelModel.SPECIAL_QUESTION_BLOCK: {
                        sprite = Assets.items[0][0];
                        imgGraphics.drawImage(Assets.level[3][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.SPIKY: {
                        sprite = Assets.enemies[0][3];
                        isEnemy = true;
                        break;
                    }
                    case MarioLevelModel.BULLET_BILL_TOP: {
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        sprite = Assets.level[3][0];
                        break;
                    }
                    case MarioLevelModel.BULLET_BILL_BOTTOM: {
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        sprite = Assets.level[4][0];
                        break;
                    }
                    case MarioLevelModel.USED_BLOCK: {
                        sprite = Assets.level[6][1];
                        break;
                    }
                    case MarioLevelModel.MARIO_EXIT: {
                        flags.add(i);
                        sprite = Assets.level[6][4];
                        break;
                    }
                    case MarioLevelModel.COIN_HIDDEN_BLOCK:{
                        sprite = Assets.level[1][2];
                        imgGraphics.drawImage(Assets.level[6][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.LIFE_HIDDEN_BLOCK:{
                        sprite = Assets.level[1][1];
                        imgGraphics.drawImage(Assets.level[6][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.GOOMBA_WINGED: {
                        sprite = Assets.enemies[0][2];
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.GREEN_KOOPA_WINGED: {
                        sprite = Assets.enemies[0][1];
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.RED_KOOPA_WINGED: {
                        sprite = Assets.enemies[0][0];
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.SPIKY_WINGED: {
                        sprite = Assets.enemies[0][3];
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.PLATFORM_BACKGROUND:{
                        sprite = Assets.level[7][5];
                        break;
                    }
                    case MarioLevelModel.PLATFORM:{
                        sprite = Assets.level[6][5];
                        break;
                    }
                    default:
                        sprite = Assets.level[6][2];
                }
                if (isEnemy) {
                    imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                    imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                } else {
                    imgGraphics.drawImage(sprite, (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                }
            }
        }
        //Dibujar banderas
        for(int i : flags){
            for(int f = FLAG_START; f < FLAG_END;f++){
                imgGraphics.drawImage(Assets.level[6][4], (i % width) * BLOCKSIZE, (BLOCKSIZE-f) * BLOCKSIZE, null);
                imgGraphics.drawImage(Assets.level[0][5], (i % width) * BLOCKSIZE, (BLOCKSIZE-f) * BLOCKSIZE, null);
            }
            imgGraphics.drawImage(Assets.level[7][4], (i % width) * BLOCKSIZE, (BLOCKSIZE - FLAG_END) * BLOCKSIZE, null);
            imgGraphics.drawImage(Assets.level[1][5], (i-1 % width) * BLOCKSIZE +8, (BLOCKSIZE - FLAG_END + 1) * BLOCKSIZE, null);
        }
        this.dibujar(img,width*BLOCKSIZE,BLOCKSIZE*BLOCKSIZE);
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
                    xShift += BLOCKSIZE; break;
            case VK_LEFT:
                if(xShift > 0)
                     xShift -= BLOCKSIZE; break;
            case VK_UP:
                if(currentLevel < levels.length-1) {
                    currentLevel++;
                    try {
                        loadLevel();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    xShift = 0;
                }
                break;
            case VK_DOWN:
                if(currentLevel > 0) {
                    currentLevel--;
                    try {
                        loadLevel();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    xShift = 0;
                }
                break;
        }
        repaint();

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class Panel extends JPanel{
        public void paintComponent(Graphics g){
            g.drawImage(img, 0 - xShift, screenSize.height/2 -BLOCKSIZE*BLOCKSIZE, levelWidth, levelHeight, null);
            g.drawString(levels[currentLevel].getName(),0,screenSize.height/2 +30);
        }
    }

    public void dibujar(Image img, int width, int height){
        this.img = img;
        this.levelWidth = width;
        this.levelHeight = height;
        repaint();
    }

}
