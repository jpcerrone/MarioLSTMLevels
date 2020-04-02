package levelGenerators.juanCerrone;

import engine.core.MarioLevelModel;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ParamAndGradientIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class LSTMNetwork {
    //Directorio que contiene el modelo de la última red generada
    private static final String MODELSAVEPATH = "model/model.zip";
    //Direcotrio que contiene el log del score de la última red generada
    private static final String LOGSAVEPATH = "log.csv";
    //Carpeta que contiene los niveles usados para entrenar
    protected File levelsFolder;
    //Cada cuantos bloques se actualizan los parametros
    protected static final int tbpttLength = 0;
    //Cantidad de cedas lstm por capa
    protected static final int lstmLayerSize = 128;
    //Seed
    private static final long seed = 12345;
    //Cantidad de epochs
    protected static final int numEpochs = 4000 ;
    //Referencia a la red
    private MultiLayerNetwork net;
    //Iterador que permite recorrer los niveles
    private LevelIterator characterIterator;
    //Generador de numero aleatorios
    private Random rng;
    //Tamaño del minibatch (Ejemplos que entrenan en paralelo)
    protected int minibatchSize = 16;
    //Learning rate
    protected double learningRate = 0.01;
    //Altura de los niveles
    private static final int LEVEL_HEIGHT = 16;

    public LSTMNetwork(String levelsFolder) {
        this.levelsFolder = new File(levelsFolder);
        rng = new Random();
        try {
            characterIterator = getLevelIterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Configura la red y realiza el entrenamiento
    public void initialize() throws IOException {
        int nOut = characterIterator.inputColumns();
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0001) //swap for dropout, puede funcionar para que deje de explotar
                .weightInit(WeightInit.XAVIER) //For tanh
                .updater(new Adam(learningRate))
                .list()
                .layer(new LSTM.Builder().nIn(characterIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
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
        //Para mostrar el score del gradient descent
        //net.setListeners(new ScoreIterationListener(200));
        //Logue los scores en un archivo
        net.setListeners(new ParamAndGradientIterationListener(1, true, false, false, false, false, true, false, new File(LOGSAVEPATH), ","));

        //Entrena la red durante la cantidad de epochs especificada y se guarda el modelo de la misma en un archivo
        for( int j=0; j<numEpochs; j++ ){
            System.out.println("Epoch: " + j );
            while(characterIterator.hasNext()){
                DataSet ds = characterIterator.next();
                net.fit(ds);
            }
            characterIterator.reset();
        }
        System.out.println("\n\nExample complete");
        net.save(new File(MODELSAVEPATH), true);
    }

    //Genera un nivel usando la red ya entrenada
    public String getGeneratedLevel(MarioLevelModel model, String initSeed){
        if(initSeed.length()%LEVEL_HEIGHT != 0){
            throw new IllegalArgumentException("initSeed debe ser múltiplo de 16");
        }

        //Se crea la primer entrada en base al seed de nivel pasado
        INDArray initializationInput = Nd4j.zeros(1,characterIterator.inputColumns(), initSeed.length()); //El 1 es como el minibatch, aca seria un solo ejemplo todo ver si cambia con el minibatch
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
        output = output.tensorAlongDimension((int)output.size(2)-1,1,0);	//Gets the last time step output
        for (int i = init.length/16; i < model.getWidth(); i++) {
            for (int j = model.getHeight() - 1; j >= 0 ; j--) {
                INDArray nextInput = Nd4j.zeros(1, characterIterator.inputColumns());
                char sample = sampleFromProbDistribution(output);
                nextInput.putScalar(new int[]{0, characterIterator.convertCharacterToIndex(sample)}, 1.0f);
                model.setBlock(i,j,sample);
                if(sample == MarioLevelModel.MARIO_EXIT){
                    return model.copyUntilFlag(i+1).getMap();
                }
                output = net.rnnTimeStep(nextInput);
            }

        }
        return model.getMap();
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
    public void loadModel(){
        File model = new File(MODELSAVEPATH);
        try {
            net = MultiLayerNetwork.load(model, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
