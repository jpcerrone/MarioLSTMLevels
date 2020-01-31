import engine.core.MarioGame;
import engine.core.MarioLevelModel;
import engine.helper.Assets;

import javax.swing.*;
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

        for(int j = 0 ; j < lines.size() ;j++){
            String line = lines.get(j);
            for(int i = 0; i < line.length();i++){
                switch (line.charAt(i)) {
                    case MarioLevelModel.GROUND:
                        ventana.dibujar((i % 150) * 16, j*16, Assets.level[1][0]);
                        break;
                    case MarioLevelModel.EMPTY:
                        ventana.dibujar((i % 150) * 16, j*16, Assets.level[6][4]);
                        break;
                    case MarioLevelModel.NORMAL_BRICK:
                        ventana.dibujar((i % 150) * 16, j*16, Assets.level[6][0]);
                        break;
                    case MarioLevelModel.COIN:
                        ventana.dibujar((i % 150) * 16, j*16, Assets.level[7][1]);
                        break;
                    case MarioLevelModel.PIPE:
                        ventana.dibujar((i % 150) * 16, j*16, Assets.level[4][2]);
                        break;
                    case MarioLevelModel.GOOMBA:
                        ventana.dibujarEnemigo((i % 150) * 16, j*16, Assets.enemies[0][2]);
                        break;

                }
            }
        }


    }
}
