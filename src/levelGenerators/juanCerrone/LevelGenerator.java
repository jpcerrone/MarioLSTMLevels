package levelGenerators.juanCerrone;

import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static engine.core.MarioLevelModel.*;

public class LevelGenerator implements MarioLevelGenerator {
    private Random numberGenerator;
    private static final int VECTOR_SIZE = 25;

    public LevelGenerator(){
        numberGenerator = new Random();
    }
    private char getBlock(List<Double> probDistribution, boolean[] oneHotVector){
        //Obtiene un bloque aleatorio de acuerdo a la distribución de probabilidades dada
        double randomValue = numberGenerator.nextDouble();
        double acum = 0;
        int i=0;
        while(i<probDistribution.size()){
            if(randomValue <= acum){
                oneHotVector[i] = true;
                return getCorrespondingBlock(i);
            }
            else{
                acum+=probDistribution.get(i);
            }
            i++;
        }
        return EMPTY;
    }

    @Override
    public String getGeneratedLevel(MarioLevelModel model, MarioTimer timer) {
        //Genera un nivel bloque por bloque
        model.clearMap();
        for(int x=0;x < model.getWidth();x++){
            for(int y=0;y < model.getHeight();y++){
                boolean[] oneHotVector = new boolean[VECTOR_SIZE];
                //Aqui se debería realizar el step de la LSTM, utilizando como parámetro el oneHotVector anterior
                //y obteniendo de la red una distribución de probabilidades para poder generar el nuevo bloque
                model.setBlock(x,y,getBlock(getSampleProbabilityDistribution(),oneHotVector));

            }
        }
        return model.getMap();
    }

    private char getCorrespondingBlock(int index){
        //Chars de los bloques correspondientes a cada índice de los arreglos de distribución de probabilidad y one-hot-vectors
        switch (index){
            case 0: return GROUND;
            case 1: return PYRAMID_BLOCK;
            case 2: return NORMAL_BRICK;
            case 3: return COIN_BRICK;
            case 4: return LIFE_BRICK;
            case 5: return SPECIAL_BRICK;
            case 6: return SPECIAL_QUESTION_BLOCK;
            case 7: return COIN_QUESTION_BLOCK;
            case 8: return COIN_HIDDEN_BLOCK;
            case 9: return LIFE_HIDDEN_BLOCK;
            case 10: return USED_BLOCK;
            case 11: return COIN;
            case 12: return PIPE;
            case 13: return PIPE_FLOWER;
            case 14: return BULLET_BILL;
            case 15: return PLATFORM_BACKGROUND;
            case 16: return PLATFORM;
            case 17: return GOOMBA;
            case 18: return GOOMBA_WINGED;
            case 19: return RED_KOOPA;
            case 20: return RED_KOOPA_WINGED;
            case 21: return GREEN_KOOPA;
            case 22: return GREEN_KOOPA_WINGED;
            case 23: return SPIKY;
            case 24: return SPIKY_WINGED;
            case 25: return EMPTY;
            default: return EMPTY;
        }

    }
    private List<Double> getSampleProbabilityDistribution(){
        //Genera una distribución de probabilidades uniformemente disrtibuida (no tendrá uso una vez implementada la red)
        List<Double> distribution = new ArrayList<>(VECTOR_SIZE);
        for(int i=0; i <VECTOR_SIZE;i++){
            distribution.add(0.04);
        }
        return distribution;
    }

    @Override
    public String getGeneratorName() {
        return "JuanCerroneGenerator";
    }
}
