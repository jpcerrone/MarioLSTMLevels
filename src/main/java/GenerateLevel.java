import engine.core.MarioGame;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.core.MarioTimer;
import levelGenerators.juanCerrone.LSTMNetwork;
import levelGenerators.juanCerrone.Seed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GenerateLevel {

	private static final boolean trainNetwork = false;
	private static final int numberOfLevels = 5;
	private static final int maxLevelWidth = 400;
	private static final String initSeed = Seed.OVERWORLD;
	private static Map<Character,Double> modifications;
	//Carpeta donde se guardan los niveles generados
	private static final String generatedLevelsFolder = "levels/jC/";

	private static void initModifications(){
		modifications = new HashMap<>();
		/*
		modifications.put(MarioLevelModel.GOOMBA,5.0);
		modifications.put(MarioLevelModel.GREEN_KOOPA,3.0);
		 */
	}

	public static void main(String[] args) throws IOException {
		LSTMNetwork network = new LSTMNetwork();
		network.initialize(trainNetwork);
		initModifications();
		for(int i=0; i < numberOfLevels;i++) {
			System.out.println("Generando nivel " + (i+1) + "...") ;
			String level = network.getGeneratedLevel(new MarioLevelModel(maxLevelWidth, 16),initSeed, modifications);
			network.saveFile(level,generatedLevelsFolder);
		}
    }
}
