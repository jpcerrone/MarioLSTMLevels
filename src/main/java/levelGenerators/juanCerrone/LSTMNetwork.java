package levelGenerators.juanCerrone;

import engine.core.MarioLevel;
import engine.core.MarioLevelModel;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
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
    private File levelsFolder;
    private static final int tbpttLength = 64;   //Cada cuantos bloque se actualizan los parametros
    private static final int lstmLayerSize = 128; //Cantidad de cedas lstm por capa
    private static final long seed = 12345;
    public static final int numEpochs = 5;
    private MultiLayerNetwork net;
    private CharacterIterator characterIterator;
    private Random rng;

    public LSTMNetwork(String levelsFolder) {
        this.levelsFolder = new File(levelsFolder);
    }

    public void initialize() throws IOException {
        rng = new Random();
        characterIterator = getLevelIterator();


        int nOut = characterIterator.inputColumns();


        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.005))
                .list()
                .layer(new LSTM.Builder().nIn(characterIterator.inputColumns()).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new LSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation(Activation.SOFTMAX)        //MCXENT + softmax for classification
                        .nIn(lstmLayerSize).nOut(nOut).build())
                .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
                .build();

        net = new MultiLayerNetwork(conf);
        net.init();
        //net.setListeners(new ScoreIterationListener(1)); //Para mostrar el score del gradient descent

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




    }

    public String getGeneratedLevel(MarioLevelModel model, String initSeed){

        //Se crea la primer entrada en base al seed de nivel pasado
        INDArray initializationInput = Nd4j.zeros(1,characterIterator.inputColumns(), initSeed.length()); //El 1 es como el minibatch, aca seria un solo ejemplo
        char[] init = initSeed.toCharArray();
        for( int i=0; i<init.length; i++ ){ //PROBAR init.lenght - 1 a ver si arregla el problema de generar el nivel más arriba
            int idx = characterIterator.convertCharacterToIndex(init[i]);
            initializationInput.putScalar(new int[]{0,idx,i}, 1.0f);
        }

        //Sample from network (and feed samples back into input) one character at a time (for all samples)
        net.rnnClearPreviousState();
        INDArray output = net.rnnTimeStep(initializationInput);
        output = output.tensorAlongDimension((int)output.size(2)-1,1,0);	//Gets the last time step output

        for (int i = 0; i < init.length; i++) { //todo Seguir aca con initSeeds mas largas

            model.setBlock(0,(15-i),init[i]);
        }
        System.out.println(model.getMap());

        //add char to level, after sampling

        for (int i = 1; i < model.getWidth(); i++) {
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

    private CharacterIterator getLevelIterator() throws IOException {
        char[] validCharacters = MarioLevelModel.getAllTiles();
        File[] levels = levelsFolder.listFiles();
        return new CharacterIterator(levels, validCharacters, new Random(12345));
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

}
