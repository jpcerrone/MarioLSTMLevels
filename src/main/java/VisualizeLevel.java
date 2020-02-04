
import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.helper.Assets;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class VisualizeLevel{

    public static void main(String[] args) throws IOException {
        Ventana ventana = new Ventana();
        ventana.setVisible(true);
        File level = new File(args[0]);
        List<String> lines = Files.readAllLines(level.toPath());
        int width = lines.get(0).length();
        BufferedImage img = new BufferedImage(width*16,16*16,BufferedImage.TYPE_INT_RGB);
        Graphics imgGraphics = img.getGraphics();
        for(int j = 0 ; j < lines.size() ;j++){
            String line = lines.get(j);
            for(int i = 0; i < width;i++){
                Image sprite;
                Boolean isSpecial = false;
                Boolean isEnemy = false;
                switch (line.charAt(i)) {
                    case MarioLevelModel.GROUND:
                        sprite = Assets.level[1][0];
                        break;
                    case MarioLevelModel.EMPTY:
                        sprite = Assets.level[6][4];
                        break;
                    case MarioLevelModel.NORMAL_BRICK:
                        sprite = Assets.level[6][0];
                        break;
                    case MarioLevelModel.COIN:
                        sprite = Assets.level[7][1];
                        break;
                    case MarioLevelModel.PIPE:
                        sprite = Assets.level[4][2];
                        break;
                    case MarioLevelModel.PIPE_FLOWER:
                        sprite = Assets.level[4][2];
                        break;
                    case MarioLevelModel.GOOMBA:
                        sprite = Assets.enemies[0][2];
                        isEnemy = true;
                        break;
                    case MarioLevelModel.PYRAMID_BLOCK:
                        sprite = Assets.level[2][0];
                        break;
                    case MarioLevelModel.GREEN_KOOPA:
                        sprite = Assets.enemies[1][0];
                        isEnemy = true;
                        break;
                    case MarioLevelModel.RED_KOOPA:
                        sprite = Assets.enemies[2][0];
                        isEnemy = true;
                        break;
                    case MarioLevelModel.SPECIAL_BRICK:
                        sprite =  Assets.items[0][0];
                        imgGraphics.drawImage( Assets.level[6][0], (i % width) * 16, j * 16, null);
                        isSpecial = true;
                        break;
                    case MarioLevelModel.LIFE_BRICK:
                        sprite =  Assets.items[1][1];
                        imgGraphics.drawImage( Assets.level[6][0], (i % width) * 16, j * 16, null);
                        isSpecial = true;
                        break;
                    case MarioLevelModel.COIN_BRICK:
                        sprite =  Assets.level[7][1];
                        imgGraphics.drawImage( Assets.level[6][0], (i % width) * 16, j * 16, null);
                        isSpecial = true;
                        break;
                    case MarioLevelModel.COIN_QUESTION_BLOCK:
                        sprite =  Assets.level[3][1];
                        break;
                    case MarioLevelModel.SPECIAL_QUESTION_BLOCK:
                        sprite =  Assets.items[0][0];
                        imgGraphics.drawImage( Assets.level[3][1], (i % width) * 16, j * 16, null);
                        isSpecial = true;
                        break;

                    default:
                        sprite = Assets.level[6][4];
                }
                    if (isEnemy) {
                        imgGraphics.drawImage(Assets.level[6][4], (i % width) * 16, j * 16, null);
                        imgGraphics.drawImage(sprite, (i % width) * 16, j * 16 - 16, null);
                    } else {
                        imgGraphics.drawImage(sprite, (i % width) * 16, j * 16, null);
                    }
            }
        }
        ventana.dibujar(img,width*16,16*16);

    }
}
