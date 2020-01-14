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
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

public class LevelIterator {
    //Caracteres válidos
    private char[] validCharacters;
    //Mapa de caracter (bloques) a un indice para los arreglos de entrada y salida
    private Map<Character,Integer> charToIdxMap;
    //Lista con todos los niveles representados como cadenas de caracteres
    private List<char[]> charLevels;
    //Lista de indices de los niveles en orden aleatorio
    private List<Integer> randomLevelList = new LinkedList<>();
    //Tamaño del minibatch para el entrenamiento
    private int miniBatchSize;

    LevelIterator(File[] files, char[] validCharacters, Random rng, int miniBatchSize) throws IOException {
        this.validCharacters = validCharacters;
        this.charLevels = new ArrayList<>();
        this.miniBatchSize = miniBatchSize;
        //Carga del arreglo de indicies
        charToIdxMap = new HashMap<>();
        for( int i=0; i<validCharacters.length; i++ )
            charToIdxMap.put(validCharacters[i], i);

        //Bottom Up Order
        //Carga el los niveles y los convierte a arreglos de caracteres ordenados de forma bottom up, agregandolos a la lista de niveles
        for(File file : files) { StringBuilder stringLevel = new StringBuilder();
            List<String> lines = Files.readAllLines(file.toPath());
            int width = lines.get(0).length();
            for (int i = 0; i < width; i++) {
                for(int j = 15; j >= 0; j--){
                    stringLevel.append(lines.get(j).charAt(i));
                }
            }
            charLevels.add(stringLevel.toString().toCharArray());
        }

        initializeRandomLevelList();
    }

    public char convertIndexToCharacter( int idx ){
        return validCharacters[idx];
    }

    public int convertCharacterToIndex( char c ){
        return charToIdxMap.get(c);
    }

    public boolean hasNext() {
        return randomLevelList.size() > 0;
    }

    public DataSet next() {
        return next(miniBatchSize);
    }

    public DataSet next(int miniBatchSize) {
        //Retorna un DataSet que contiene el próximo nivel como input y los labels correspondientes a cada caracter

        //Se utiliza para que el ultimo batch contenga los ejemplos que sobran
        int currMinibatchSize = Math.min(miniBatchSize, randomLevelList.size());

        int maxLevelLenght = 0;
        for(char[] level : charLevels){
            maxLevelLenght = Math.max(level.length,maxLevelLenght);
        }

        // dimension 0 = number of examples in minibatch
        // dimension 1 = size of each vector (i.e., number of characters)
        // dimension 2 = length of each time series/example
        //Why 'f' order here? See http://deeplearning4j.org/usingrnns.html#data section "Alternative: Implementing a custom DataSetIterator"

        INDArray input = Nd4j.create(new int[]{miniBatchSize,validCharacters.length,maxLevelLenght}, 'f');
        INDArray labels = Nd4j.create(new int[]{miniBatchSize,validCharacters.length,maxLevelLenght}, 'f');

        //Arreglos que serán utilizados para el Masking, inicializados en cero
        INDArray featuresMask = Nd4j.zeros(miniBatchSize, maxLevelLenght);
        INDArray labelsMask = Nd4j.zeros(miniBatchSize, maxLevelLenght);


        for(int m=0; m < miniBatchSize;m++) {
            int levelIndex = randomLevelList.remove(randomLevelList.size()-1);
            int currCharIdx = charToIdxMap.get(charLevels.get(levelIndex)[0]);    //Caracter de entrada actual
            for (int i = 0; i < maxLevelLenght; i++) {
                if(i < charLevels.get(levelIndex).length) { //agregado
                    int nextCharIdx = charToIdxMap.get(charLevels.get(levelIndex)[i]);
                    //Proximo caracter a predecir
                    input.putScalar(new int[]{m, currCharIdx, i}, 1.0);
                    labels.putScalar(new int[]{m, nextCharIdx, i}, 1.0);
                    //Lo de abajo agregado
                    featuresMask.putScalar(new int[]{m,i}, 1.0);
                    labelsMask.putScalar(new int[]{m,i}, 1.0);
                    currCharIdx = nextCharIdx;
                }
            }
        }
            return new DataSet(input, labels,featuresMask,labelsMask);
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
        initializeRandomLevelList();
    }

    private void initializeRandomLevelList() {
        for(int i = 0; i < charLevels.size();i++)
            randomLevelList.add(i);
        Collections.shuffle(randomLevelList, new Random());
    }

}
