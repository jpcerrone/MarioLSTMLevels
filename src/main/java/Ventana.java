import engine.core.MarioLevelModel;
import engine.core.MarioSprite;
import engine.core.MarioWorld;
import engine.graphics.MarioImage;
import engine.helper.Assets;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;

public class Ventana extends JFrame implements KeyListener, ActionListener {
    //Dimensiones de la pantalla
    private Dimension screenSize;
    //Imágen sobre la cual se dibujan los niveles
    private Image img;
    //Ancho y alto del nivel
    private int levelWidth;
    private int levelHeight;
    //Desplazamiento del nivel en el eje X, para poder scrollear el nivel
    private int xShift;
    //Carpeta que contiene los niveles a dibujar
    private static final String levelsFolderPath = "levels/jC";
    //Imágen de salida
    private static final String outuptImageFileName = "levelImage.png";
    //Nivel actual
    private int currentLevel;
    //Arreglo que contiene todos los nvieles
    private File[] levels;
    //Tamaño en píxeles de cada bloque
    private static final int BLOCKSIZE = 16;
    //Posicion en y donde comienza la bandera
    private static final int FLAG_START = 4;
    //Posicion en y donde termina la bandera
    private static final int FLAG_END = 14;
    //Color del cielo
    private static final Color skyColor = new Color(103,175,252);
    //Ubicación alas con respecto al sprite del enemigo
    private static final int wingOffset = 8;
    //Tamaño de las flechas
    private int arrowSize;

    private void initGraphics(){
        xShift = 0;
        currentLevel = 0;
        Assets.init(getGraphicsConfiguration());
        arrowSize = Assets.arrows[0][0].getWidth(null);
    }

    private void initMenu(){
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Exportar");
        menuBar.add(menu);
        JMenuItem exportPng = new JMenuItem("Exportar png");
        menu.add(exportPng);
        exportPng.addActionListener(this);
        setJMenuBar(menuBar);
    }

    //Inicializa la ventana y carga el primer nivel
    Ventana() throws IOException {
        super("Visualizador");
        initMenu();
        JPanel panel = new Panel();
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width,screenSize.height);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        File levelFolder = new File(levelsFolderPath);
        levels = levelFolder.listFiles();
        initGraphics();
        loadLevel();
        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
    }

    //Carga un nuevo nivel
    private void loadLevel() throws IOException {
        File level = levels[currentLevel];
        List<String> lines = Files.readAllLines(level.toPath());
        int width = lines.get(0).length();
        BufferedImage img = new BufferedImage(width*BLOCKSIZE,BLOCKSIZE*BLOCKSIZE,BufferedImage.TYPE_INT_RGB);
        Graphics imgGraphics = img.getGraphics();
        //Dibujar el cielo
        imgGraphics.setColor(skyColor);
        imgGraphics.fillRect(0,0,width*BLOCKSIZE,BLOCKSIZE*BLOCKSIZE);
        List<Integer> flags = new ArrayList<>(); //Lista de las cordenadas de las banderas
        boolean rightPipe = false;
        //Dibuja cada bloque dependiendo de su tipo
        for(int j = 0 ; j < lines.size() ;j++){
            String line = lines.get(j);
            for(int i = 0; i < width;i++){
                switch (line.charAt(i)) {
                    //Bloques:
                    case MarioLevelModel.MARIO_START:{
                        imgGraphics.drawImage(Assets.smallMario[1][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.GROUND: {
                        imgGraphics.drawImage(Assets.level[1][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.NORMAL_BRICK: {
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.COIN: {
                        imgGraphics.drawImage(Assets.level[7][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.PIPE_FLOWER:{
                            if (line.charAt(i - 1) != MarioLevelModel.PIPE_FLOWER && line.charAt(i - 1) != MarioLevelModel.PIPE) {
                                if (j > 0 && lines.get(j - 1).charAt(i) != MarioLevelModel.PIPE && lines.get(j - 1).charAt(i) != MarioLevelModel.PIPE_FLOWER) {
                                    //Flor
                                    imgGraphics.drawImage(Assets.enemies[1][6], (i % width) * BLOCKSIZE + BLOCKSIZE / 2, j * BLOCKSIZE - (int) (1.7 * BLOCKSIZE), null);
                                    imgGraphics.drawImage(Assets.level[2][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                } else {
                                    imgGraphics.drawImage(Assets.level[4][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                rightPipe = true;
                            }
                        else{
                            if(rightPipe){
                                if(j > 0 && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE_FLOWER) {
                                    imgGraphics.drawImage(Assets.level[3][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                else{
                                    imgGraphics.drawImage(Assets.level[5][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                rightPipe = false;
                            }
                            else{
                                if(j > 0 && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE_FLOWER){
                                    imgGraphics.drawImage(Assets.level[2][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                else {
                                    imgGraphics.drawImage(Assets.level[4][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                rightPipe = true;
                            }
                        }

                        break;
                    }
                    case MarioLevelModel.PIPE:{
                        if(line.charAt(i-1) != MarioLevelModel.PIPE_FLOWER && line.charAt(i-1) != MarioLevelModel.PIPE) {
                            if(j > 0 && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE_FLOWER){
                                imgGraphics.drawImage(Assets.level[2][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                            }
                            else {
                                imgGraphics.drawImage(Assets.level[4][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                            }
                            rightPipe = true;
                        }
                        else{
                            if(rightPipe){
                                if(j > 0 && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE_FLOWER) {
                                    imgGraphics.drawImage(Assets.level[3][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                else{
                                    imgGraphics.drawImage(Assets.level[5][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                rightPipe = false;
                            }
                            else{
                                if(j > 0 && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE && lines.get(j-1).charAt(i) != MarioLevelModel.PIPE_FLOWER){
                                    imgGraphics.drawImage(Assets.level[2][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                else {
                                    imgGraphics.drawImage(Assets.level[4][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                                }
                                rightPipe = true;
                            }
                        }
                        break;
                    }
                    case MarioLevelModel.PYRAMID_BLOCK: {
                        imgGraphics.drawImage(Assets.level[2][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.SPECIAL_BRICK: {
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.items[0][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.LIFE_BRICK: {
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.items[1][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.COIN_BRICK: {
                        imgGraphics.drawImage(Assets.level[6][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.level[7][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.COIN_QUESTION_BLOCK: {
                        imgGraphics.drawImage(Assets.level[3][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.SPECIAL_QUESTION_BLOCK: {
                        imgGraphics.drawImage(Assets.level[3][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.items[0][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.BULLET_BILL_TOP: {
                        imgGraphics.drawImage(Assets.level[4][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.BULLET_BILL_BOTTOM: {
                        imgGraphics.drawImage(Assets.level[3][0], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.USED_BLOCK: {
                        imgGraphics.drawImage(Assets.level[6][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.MARIO_EXIT: {
                        flags.add(i);
                        break;
                    }
                    case MarioLevelModel.COIN_HIDDEN_BLOCK:{
                        imgGraphics.drawImage(Assets.level[6][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.level[1][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.LIFE_HIDDEN_BLOCK:{
                        imgGraphics.drawImage(Assets.level[6][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.level[1][1], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.PLATFORM_BACKGROUND:{
                        imgGraphics.drawImage(Assets.level[7][5], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.PLATFORM:{
                        imgGraphics.drawImage(Assets.level[6][5], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.EMPTY:{
                        //do nothing
                        break;
                    }
                    //Enemigos:
                    //Algunas de las imágenes de los enemigos son dadas vuelta ya que las originales no estan en la dirección deseada
                    case MarioLevelModel.GOOMBA_WINGED: {
                        imgGraphics.drawImage(Assets.enemies[0][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.GREEN_KOOPA_WINGED: {
                        imgGraphics.drawImage(Assets.enemies[0][1], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        imgGraphics.drawImage(Assets.enemies[1][4], (i % width) * BLOCKSIZE + BLOCKSIZE+wingOffset, j * BLOCKSIZE - BLOCKSIZE -wingOffset,-BLOCKSIZE, BLOCKSIZE*2, null);
                        break;
                    }
                    case MarioLevelModel.RED_KOOPA_WINGED: {
                        imgGraphics.drawImage(Assets.enemies[0][0], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        imgGraphics.drawImage(Assets.enemies[1][4], (i % width) * BLOCKSIZE + BLOCKSIZE+wingOffset, j * BLOCKSIZE - BLOCKSIZE -wingOffset,-BLOCKSIZE, BLOCKSIZE*2, null);
                        break;
                    }
                    case MarioLevelModel.SPIKY_WINGED: {
                        imgGraphics.drawImage(Assets.enemies[0][3], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        imgGraphics.drawImage(Assets.enemies[0][4], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.SPIKY: {
                        imgGraphics.drawImage(Assets.enemies[0][3], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        break;
                    }
                    case MarioLevelModel.GOOMBA: {
                        imgGraphics.drawImage(Assets.enemies[0][2], (i % width) * BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE, null);
                        break;
                    }
                    case MarioLevelModel.GREEN_KOOPA: {
                        imgGraphics.drawImage(Assets.enemies[0][1], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        break;
                    }
                    case MarioLevelModel.RED_KOOPA: {
                        imgGraphics.drawImage(Assets.enemies[0][0], (i % width) * BLOCKSIZE + BLOCKSIZE, j * BLOCKSIZE - BLOCKSIZE,-BLOCKSIZE, BLOCKSIZE*2, null);
                        break;
                    }
                    default:{
                        imgGraphics.drawImage(Assets.level[0][3], (i % width) * BLOCKSIZE, j * BLOCKSIZE, null);
                        break;
                    }
                }
            }
        }
        //Dibujar banderas
        for(int i : flags){
            for(int f = FLAG_START; f < FLAG_END;f++){
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
    //Control de teclado
    public void keyPressed(KeyEvent e) {
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

    @Override
    public void actionPerformed(ActionEvent e) {
        //Guardar imágen
        File imgFile = new File("outuptImageFileName");
        try {
            ImageIO.write((RenderedImage) img,"png",imgFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private class Panel extends JPanel{
        public void paintComponent(Graphics g){
            //nivel
            int startingX;
            if(img.getWidth(null) < screenSize.width)
                startingX = (screenSize.width-img.getWidth(null))/2;
            else
                startingX = -xShift;
            g.drawImage(img, startingX, screenSize.height/2 -BLOCKSIZE*BLOCKSIZE, levelWidth, levelHeight, null);
            //flechas/texto
            g.drawString(levels[currentLevel].getName(),screenSize.width/2,screenSize.height/2 +arrowSize/2);
            g.drawImage(Assets.arrows[2][0],screenSize.width -2*arrowSize,screenSize.height- 4*arrowSize,null);
            g.drawImage(Assets.arrows[0][0],screenSize.width -3*arrowSize,screenSize.height- 4*arrowSize,null);
            g.drawImage(Assets.arrows[1][0],arrowSize,screenSize.height- 4*arrowSize,null);
            g.drawImage(Assets.arrows[3][0],2*arrowSize,screenSize.height- 4*arrowSize,null);
            g.drawImage(Assets.text[0][0],arrowSize,screenSize.height- 3*arrowSize,null);
            g.drawImage(Assets.text[0][1],screenSize.width - arrowSize*3,screenSize.height- 3*arrowSize,null);
        }
    }

    public void dibujar(Image img, int width, int height){
        this.img = img;
        this.levelWidth = width;
        this.levelHeight = height;
        repaint();
    }

}
