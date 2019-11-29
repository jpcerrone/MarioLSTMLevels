package levelGenerators.juanCerrone;

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
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LSTMNetwork {
    private File levelsFolder;
    private static final int sequenceLenght = 150; //Tamaño de la secuencia de cada ejemplo
    private static final int miniBatchSize = 32; //Cantidad de ejemplos que se le alimentará a la red en cada next()
    private static final int tbpttLength = 50;   //Cada cuantos bloque se actualizan los parametros
    private static final int lstmLayerSize = 100;
    private static final long seed = 12345;
    private static final int numEpochs = 10;
    public LSTMNetwork(String levelsFolder) {
        this.levelsFolder = new File(levelsFolder);
    }

    public void initialize() throws IOException {

        CharacterIterator characterIterator = getLevelIterator(miniBatchSize,sequenceLenght);
        int i = 0;
        while(characterIterator.hasNext()){
            characterIterator.next();
            i++;
        }
        System.out.println("Ejemplos: " + i);
        int nOut = characterIterator.inputColumns();

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
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

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1)); //Para mostrar el score del gradient descent

        //Print the  number of parameters in the network (and for each layer)
        System.out.println(net.summary());

        for( int j=0; j<numEpochs; j++ ){
            while(characterIterator.hasNext()){
                DataSet ds = characterIterator.next();
                net.fit(ds);
            }
            //System.out.println("Epoch: " + j );
            characterIterator.reset();
        }

        System.out.println("\n\nExample complete");

    }


    private CharacterIterator getLevelIterator(int miniBatchSize, int sequenceLength) throws IOException {
        char[] validCharacters = MarioLevelModel.getAllTiles();
        File[] levels = levelsFolder.listFiles();
        return new CharacterIterator(levels, StandardCharsets.UTF_8,
                miniBatchSize, sequenceLength, validCharacters, new Random(12345));
    }
}
