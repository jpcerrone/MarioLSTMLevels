package levelGenerators.juanCerrone;

/* *****************************************************************************
 * Copyright (c) 2015-2019 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class CharacterIterator implements DataSetIterator {
    //Valid characters
    private char[] validCharacters;
    //Maps each character to an index ind the input/output
    private Map<Character,Integer> charToIdxMap;
    //All characters of the input file (after filtering to only those that are valid
    private List<char[]> charLevels;
    //Length of each example/minibatch (number of characters)
    private Random rng;
    //Offsets for the start of each example
    private List<Integer> randomLevelList = new LinkedList<>();


    CharacterIterator(File[] files, char[] validCharacters, Random rng) throws IOException {
        this.validCharacters = validCharacters;
        this.charLevels = new ArrayList<>();
        this.rng = rng;

        //Store valid characters is a map for later use in vectorization
        charToIdxMap = new HashMap<>();
        for( int i=0; i<validCharacters.length; i++ )
            charToIdxMap.put(validCharacters[i], i);


        //Bottom Up Order
        //Carga el los archivos y los convierte a char[]
        for(File file : files) { StringBuilder stringLevel = new StringBuilder();
            //Copio el archivo a una lista de strings con cada linea
            List<String> lines = Files.readAllLines(file.toPath());
            int width = lines.get(0).length();
            //Crea el arreglo full level que contiene el nivel ordenado de manera bottom up columna x columna
            String fullLevel = "";
            for (int i = 0; i < width; i++) {
                for(int j = 15; j >= 0; j--){
                    stringLevel.append(lines.get(j).charAt(i));
                }
            }
            charLevels.add(stringLevel.toString().toCharArray());
        }
        initializeOffsets();
    }

    public char convertIndexToCharacter( int idx ){
        return validCharacters[idx];
    }

    public int convertCharacterToIndex( char c ){
        return charToIdxMap.get(c);
    }

    public char getRandomCharacter(){
        return validCharacters[(int) (rng.nextDouble()*validCharacters.length)];
    }

    public boolean hasNext() {
        return randomLevelList.size() > 0;
    }

    public DataSet next() {
        return next(0);
    }

    public DataSet next(int num) {
        //Allocate space:
        //Note the order here:
        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters)
        // dimension 2 = length of each time series/example
        //Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a custom DataSetIterator"
        int levelIndex = randomLevelList.remove(randomLevelList.size()-1);
        INDArray input = Nd4j.create(new int[]{1,validCharacters.length,charLevels.get(levelIndex).length}, 'f');
        INDArray labels = Nd4j.create(new int[]{1,validCharacters.length,charLevels.get(levelIndex).length}, 'f');

        int currCharIdx = charToIdxMap.get(charLevels.get(levelIndex)[0]);	//Current input
        for( int i=0; i<charLevels.get(levelIndex).length; i++){
            int nextCharIdx = charToIdxMap.get(charLevels.get(levelIndex)[i]);		//Next character to predict
            input.putScalar(new int[]{0,currCharIdx,i}, 1.0);
            labels.putScalar(new int[]{0,nextCharIdx,i}, 1.0);
            currCharIdx = nextCharIdx;
        }
        return new DataSet(input,labels);
    }

    private int totalExamples() {
        return charLevels.size();
    }

    public int inputColumns() {
        return validCharacters.length;
    }

    public int totalOutcomes() {
        return validCharacters.length;
    }

    public void reset() {
        randomLevelList.clear();
        initializeOffsets();
    }

    @Override
    public int batch() {
        return 0;
    }

    private void initializeOffsets() {
        for(int i = 0; i < charLevels.size();i++)
            randomLevelList.add(i);
        Collections.shuffle(randomLevelList, rng);

    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }


    @SuppressWarnings("unused")
    public int cursor() {
        return totalExamples();
    }

    @SuppressWarnings("unused")
    public int numExamples() {
        return totalExamples();
    }

    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getLabels() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
