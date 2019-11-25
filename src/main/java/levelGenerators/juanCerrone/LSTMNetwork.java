package levelGenerators.juanCerrone;

import engine.core.MarioLevelModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LSTMNetwork {
    private File levelsFolder;
    private static final int sequenceLenght = 100; //Tamaño de la secuencia de cada ejemplo
    private static final int miniBatchSize = 1; //Cantidad de ejemplos que se le alimentará a la red en cada next()

    public LSTMNetwork(String levelsFolder) {
        this.levelsFolder = new File(levelsFolder);
    }

    public void initialize() {
        try {
            CharacterIterator characterIterator = getLevelIterator(miniBatchSize,sequenceLenght);
            int i = 0;
            while(characterIterator.hasNext()){
                characterIterator.next();
                i++;
            }
            System.out.println("Ejemplos: " + i);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Aca configuraria la red
    }


    private CharacterIterator getLevelIterator(int miniBatchSize, int sequenceLength) throws IOException {
        char[] validCharacters = MarioLevelModel.getAllTiles();
        File[] levels = levelsFolder.listFiles();
        return new CharacterIterator(levels, StandardCharsets.UTF_8,
                miniBatchSize, sequenceLength, validCharacters, new Random(12345));
    }
}
