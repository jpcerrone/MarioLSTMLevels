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
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LSTMNetwork {
    private static final String MODELSAVEPATH = "model/model.zip";
    private static final String LOGSAVEPATH = "log.csv";

    protected File levelsFolder;  //Carpeta que contiene los niveles usados para entrenar
    protected static final int tbpttLength = 0;  //Cada cuantos bloques se actualizan los parametros
    protected static final int lstmLayerSize = 128;   //Cantidad de cedas lstm por capa
    private static final long seed = 12345;
    protected static final int numEpochs = 1000;  //Cantidad de epochs
    private MultiLayerNetwork net;
    private LevelIterator characterIterator;
    private Random rng;
    protected int minibatchSize = 1;
    protected double learningRate = 0.01;

    public LSTMNetwork(String levelsFolder) {
        this.levelsFolder = new File(levelsFolder);
        rng = new Random();
        try {
            characterIterator = getLevelIterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() throws IOException {

        int nOut = characterIterator.inputColumns();
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                //.l2(0.0001) //swap for dropout, puede funcionar para que deje de explotar
                .weightInit(WeightInit.XAVIER) //For tanh
                .updater(new Adam(learningRate))
                //0.005 og
                //0.000001 avg150 in 30
                //0.00001 avg 150 in 5
                //0.0001 avg 150 in 2
                .list()
                .layer(new LSTM.Builder().nIn(characterIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                /*.layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())*/
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build())
                //.backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .build();
        if(tbpttLength != 0){
            conf.setBackpropType(BackpropType.TruncatedBPTT);
            conf.setTbpttBackLength(tbpttLength);
        }

        net = new MultiLayerNetwork(conf);
        net.init();
        //net.setListeners(new ScoreIterationListener(200)); //Para mostrar el score del gradient descent
        net.setListeners(new ParamAndGradientIterationListener(1, true, false, false, false, false, true, false, new File(LOGSAVEPATH), ","));
        //Print the  number of parameters in the network (and for each layer)
        //System.out.println(net.summary());
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

    public String getGeneratedLevel(MarioLevelModel model, String initSeed){
        if(initSeed.length()%16 != 0){
            throw new IllegalArgumentException("initSeed lenght be a multiple of 16");
        }

        //Se crea la primer entrada en base al seed de nivel pasado
        INDArray initializationInput = Nd4j.zeros(1,characterIterator.inputColumns(), initSeed.length()); //El 1 es como el minibatch, aca seria un solo ejemplo
        char[] init = initSeed.toCharArray();
        for( int i=0; i<init.length; i++ ){ //PROBAR init.lenght - 1 a ver si arregla el problema de generar el nivel más arriba
            int idx = characterIterator.convertCharacterToIndex(init[i]);
            initializationInput.putScalar(new int[]{0,idx,i}, 1.0f);
        }
        for (int i = 0; i < init.length; i++) {
            model.setBlock(i/16 ,(16-(i%16)) - 1,init[i]);
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

    public void loadModel(){
        File model = new File(MODELSAVEPATH);
        try {
            net = MultiLayerNetwork.load(model, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
