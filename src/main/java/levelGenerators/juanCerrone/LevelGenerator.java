package levelGenerators.juanCerrone;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;



public class LevelGenerator implements MarioLevelGenerator {
    //Red LSTM
    private LSTMNetwork network;
    //Carpeta donde se encuentran los niveles para el entrenamiento
    private static final String trainingLevelsFolder = "levels/original/";
    //Carpeta donde se guardan los niveles generados
    private static final String generatedLevelsFolder = "levels/jC/";
    //Variable para guardar el tiempo de entrenamiento
    private long trainingTime;

    //Constructor que genera un nivel, si train es true entrena la red antes de hacerlo, si no, se genera el nivel en base a última red generada
    public LevelGenerator(boolean train) {
        network = new LSTMNetwork(trainingLevelsFolder);
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
        String level =  network.getGeneratedLevel(model,Seed.OVERWORLD);
        System.out.println(level);
        saveFile(level);
        return level;

    }

    //Guarda el archivo del nivel con las stats del mismo en su nombre
    private void saveFile(String level){
        File trainingLevelsFolderFile = new File(trainingLevelsFolder);
        File generatedLevelsFolderFile = new File(generatedLevelsFolder);
        try {
            String filename = generatedLevelsFolder
                    + "(" + generatedLevelsFolderFile.listFiles().length + ") " +
                    + network.getScore() + " score "
                    + LSTMNetwork.numEpochs + "epochs "
                    + network.minibatchSize + "batches "
                    + LSTMNetwork.lstmLayerSize + "blocks "
                    + LSTMNetwork.tbpttLength + "tbptt "
                    + trainingTime/1000 /60 + "min "
                    + trainingLevelsFolderFile.listFiles().length + "levels "
                    + network.learningRate + " lr"
                    +  ".txt";
            FileWriter f = new FileWriter(filename);
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
