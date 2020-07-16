package levelGenerators.juanCerrone;

import engine.core.MarioLevelModel;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.deeplearning4j.ui.model.storage.FileStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class LSTMNetwork {
    //Directorio que contiene el modelo de la última red generada
    private static final String MODELSAVEPATH = "model/model15oF.zip";
    //Direcotrio que contiene el log del score de la última red generada
    private static final String LOGSAVEPATH = "log";
    //Carpeta que contiene los niveles usados para entrenar
    private File levelsFolder;
    //Cada cuantos bloques se actualizan los parametros
    private static final int tbpttLength = 0;
    //Dimensionalidad del cell state
    private static final int lstmLayerSize = 128;
    //Semilla
    private static final long seed = 12345;
    //Cantidad de epochs
    private static final int numEpochs = 2000 ;
    //Referencia a la red
    private MultiLayerNetwork net;
    //Iterador que permite recorrer los niveles
    private LevelIterator characterIterator;
    //Generador de numero aleatorios
    private Random rng;
    //Tamaño del minibatch (Ejemplos que entrenan en paralelo)
    private static final int minibatchSize = 20;
    //Tasa de aprendizaje
    protected static final double learningRate = 0.01;
    //Altura de los niveles
    private static final int LEVEL_HEIGHT = 16;
    //Checkpoints de cuando generar niveles durante el entrenamiento
    private static final List<Integer> checkpoints = new ArrayList<>(List.of(1,10,50,100,1000,2000,3000,4000));
    //Carpeta donde se encuentran los niveles para el entrenamiento
    private static final String trainingLevelsFolder = "levels/original/";


    private static final String generatedDuringTrainingLevelsFolder = "levels/generatedDuringTraining/";
    private static final String defaultSeed = Seed.OVERWORLD;


    public LSTMNetwork() {
        this.levelsFolder = new File(trainingLevelsFolder);
        rng = new Random();
        try {
            characterIterator = getLevelIterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Configura la red y realiza el entrenamiento
    public void initialize(boolean train) throws IOException {
        int nOut = characterIterator.inputColumns();
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER) //Para tanh
                .updater(new Adam(learningRate))
                .gradientNormalization(GradientNormalization.ClipL2PerLayer)
                .list()
                .layer(new LSTM.Builder().nIn(characterIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).dropOut(0.8).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)
                        .nIn(lstmLayerSize).nOut(nOut).build())
                .build();
        //Si se quiere usar tbtt se debe indicar un numero mayor a 0 en tal parámetro
        if(tbpttLength != 0){
            conf.setBackpropType(BackpropType.TruncatedBPTT);
            conf.setTbpttBackLength(tbpttLength);
        }

        net = new MultiLayerNetwork(conf);
        net.init();
        if (train) {
            try {
                long trainingTime = System.currentTimeMillis();
                trainNetwork();
                trainingTime = System.currentTimeMillis() - trainingTime;
                System.out.println("Red Entrenada!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            loadModel();
    }


    private void trainNetwork() throws IOException {
        //Para mostrar el score del gradient descent
        //net.setListeners(new ScoreIterationListener(200));
        //Logue los scores en un archivo
        //net.setListeners(new ParamAndGradientIterationListener(1, true, false, false, false, false, true, false, new File(LOGSAVEPATH), ","));
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new FileStatsStorage(new File(LOGSAVEPATH));         //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        net.setListeners(new StatsListener(statsStorage));
        //Entrena la red durante la cantidad de epochs especificada y se guarda el modelo de la misma en un archivo
        for( int j=0; j<numEpochs; j++ ){
            System.out.println("Epoch: " + j );
            while(characterIterator.hasNext()){
                DataSet ds = characterIterator.next();
                net.fit(ds);
            }
            characterIterator.reset();
            if(checkpoints.contains(numEpochs)){
                String level = getGeneratedLevel(new MarioLevelModel(1000,16),defaultSeed,null);
                saveFile(level,generatedDuringTrainingLevelsFolder);
            }
        }
        System.out.println("\n\nEntrenamiento Completado");
        net.save(new File(MODELSAVEPATH), true);
    }
    //Genera un nivel usando la red ya entrenada
    public String getGeneratedLevel(MarioLevelModel model, String initSeed, Map<Character,Double> modifications){
        model.clearMap();
        if(initSeed.length()%LEVEL_HEIGHT != 0){
            throw new IllegalArgumentException("initSeed debe ser múltiplo de 16");
        }

        //Se crea la primer entrada en base al seed de nivel pasado
        INDArray initializationInput = Nd4j.zeros(1,characterIterator.inputColumns(), initSeed.length()); //El 1 es como el minibatch, aca seria un solo ejemplo
        char[] init = initSeed.toCharArray();
        for( int i=0; i<init.length; i++ ){
            int idx = characterIterator.convertCharacterToIndex(init[i]);
            initializationInput.putScalar(new int[]{0,idx,i}, 1.0f);
        }
        for (int i = 0; i < init.length; i++) {
            model.setBlock(i/LEVEL_HEIGHT ,(LEVEL_HEIGHT-(i%LEVEL_HEIGHT)) - 1,init[i]);
        }

        //Loop de generacion del nivel con la red de a un caracter por vez
        net.rnnClearPreviousState();
        INDArray output = net.rnnTimeStep(initializationInput);
        output = output.tensorAlongDimension((int)output.size(2)-1,1,0); //Obtiene la ultima salida
        for (int i = init.length/16; i < model.getWidth(); i++) {
            for (int j = model.getHeight() - 1; j >= 0 ; j--) {
                INDArray nextInput = Nd4j.zeros(1, characterIterator.inputColumns());
                char sample = sampleFromProbDistribution(output);
                if(modifications == null || modifications.isEmpty()){
                    model.setBlock(i,j,sample);
                }
                else{ //Si hay modificaciones la red genera en base a las mismas
                    for(Character c : modifications.keySet()){
                        raiseProbabilities(output,c,modifications.get(c));
                    }
                    char modifiedSample = sampleFromProbDistribution(output);
                    model.setBlock(i,j,modifiedSample);
                }
                nextInput.putScalar(new int[]{0, characterIterator.convertCharacterToIndex(sample)}, 1.0f); //La red ignorará los bloques generados mediante las modificaciones y considerará que se han generado siguiendo la distribución original
                if(sample == MarioLevelModel.MARIO_EXIT){
                    return model.copyUntilFlag(i+1).getMap();
                }
                output = net.rnnTimeStep(nextInput);
            }
        }
        return model.getMap();
    }

    private void raiseProbabilities(INDArray output,char block, double root){
        int blockIndex = characterIterator.convertCharacterToIndex(block);
        double initialRemaining = 1.0-output.getDouble(blockIndex);
        double pow = Math.pow(output.getDouble(blockIndex), 1.0 / root);
        output.putScalar(blockIndex,pow);
        double finalRemaining = 1.0-output.getDouble(blockIndex);
        double scalingRatio = finalRemaining/initialRemaining;
        for (int i = 0; i < output.length()-1; i++) {
             if(i!=blockIndex){
                 output.putScalar(i,output.getDouble(i)*scalingRatio);
             }
        }
    }
    private LevelIterator getLevelIterator() throws IOException {
        char[] validCharacters = MarioLevelModel.getAllTiles();
        File[] levels = levelsFolder.listFiles();
        return new LevelIterator(levels, validCharacters, new Random(seed),minibatchSize);
    }

    private char sampleFromProbDistribution(INDArray distribution){
        INDArray cumSum = distribution.cumsum(0); //Obtiene la frecuencia acumulada
        double randomNumber = rng.nextDouble();
        int i = 0;
        while(cumSum.getDouble(i) <= randomNumber ){
            i++;
        }
        return characterIterator.convertIndexToCharacter(i);
    }

    public double getScore(){
        return net.score();
    }

    //Carga el modelo de red de un archivo
    private void loadModel(){
        File model = new File(MODELSAVEPATH);
        try {
            net = MultiLayerNetwork.load(model, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Guarda el archivo del nivel con las stats del mismo en su nombre
    public void saveFile(String level,String folder){
        File generatedLevelsFolderFile = new File(folder);
        try {
            String filename = folder
                    + Objects.requireNonNull(generatedLevelsFolderFile.listFiles()).length
                    +  ".txt";
            FileWriter f = new FileWriter(filename);
            f.write(level);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
