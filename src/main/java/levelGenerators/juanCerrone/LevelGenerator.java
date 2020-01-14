package levelGenerators.juanCerrone;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static engine.core.MarioLevelModel.*;

public class LevelGenerator implements MarioLevelGenerator {
    private LSTMNetwork network;
    private static final String levelsFolder = "levels/original";

    public LevelGenerator() {
        network = new LSTMNetwork(levelsFolder);
        try {
            network.initialize();
            System.out.println("Initialized!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {

        //Genera un nivel bloque por bloque
        model.clearMap();
        String level =  network.getGeneratedLevel(model,"XX--------------XX--------------XX--------------XX--------------");
        System.out.println(level);

        try {
            FileWriter f = new FileWriter("levels/jC/" + network.numEpochs +"epochs " + DateTime.now().minuteOfDay().getAsText() +  ".txt");
            f.write(level);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return level;


    }


    @Override
    public String getGeneratorName() {
        return "JuanCerroneGenerator";
    }
}
