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
    //Red LSTM
    private LSTMNetwork network;
    //Carpeta donde se encuentran los niveles para el entrenamiento
    private static final String levelsFolder = "levels/original";
    //Variable para guardar el tiempo de entrenamiento
    private long trainingTime;

    //Constructor que genera un nivel, si train es true entrena la red antes de hacerlo, si no, se genera el nivel en base a última red generada
    public LevelGenerator(boolean train) {
        network = new LSTMNetwork(levelsFolder);
        if (train) {
        try {
            trainingTime = System.currentTimeMillis();
            network.initialize();
            trainingTime = System.currentTimeMillis()-trainingTime;
            System.out.println("Initialized!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
        else
            network.loadModel();

    }

    //Genera un nivel bloque por bloque y se guarda en un archivo. Se le envía un seed que describe el comienzo del nivel
    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        model.clearMap();
        String level =  network.getGeneratedLevel(model,"XX--------------XX--------------XX--------------XX--------------");
        System.out.println(level);
        saveFile(level);
        return level;

    }

    //Guarda el archivo del nivel con las stats del mismo en su nombre
    private void saveFile(String level){
        try {
            FileWriter f = new FileWriter("levels/jC/"
                    + network.getScore() + "score "
                    + LSTMNetwork.numEpochs + "epochs "
                    + network.minibatchSize + "batches "
                    + LSTMNetwork.lstmLayerSize + "blocks "
                    + LSTMNetwork.tbpttLength + "tbptt "
                    + trainingTime/1000 /60 + "min "
                    + network.levelsFolder.listFiles().length + "levels "
                    + network.learningRate + " lr"
                    +  ".txt");
            f.write(level);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getGeneratorName() {
        return "JuanCerroneGenerator";
    }
}
