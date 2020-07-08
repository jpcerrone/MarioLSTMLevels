import engine.core.MarioGame;
import engine.core.MarioLevelGenerator;
import engine.core.MarioLevelModel;
import engine.core.MarioResult;
import engine.core.MarioTimer;
import levelGenerators.juanCerrone.Seed;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GenerateLevel {

	private static final boolean trainNetwork = false;
	private static final int numberOfLevels = 5;
	private static final int maxLevelWidth = 400;
	private static final String initSeed = Seed.OVERWORLD;
	private static Map<Character,Double> modifications;

	private static void initModifications(){
		modifications = new HashMap<>();
		modifications.put(MarioLevelModel.GOOMBA,5.0);
		modifications.put(MarioLevelModel.GREEN_KOOPA,3.0);
	}

	public static void main(String[] args) {
		levelGenerators.juanCerrone.LevelGenerator generator = new levelGenerators.juanCerrone.LevelGenerator(trainNetwork);
		initModifications();
		for(int i=0; i < numberOfLevels;i++) {
			System.out.println("Generando nivel " + (i+1) + "...") ;
			String level = generator.getGeneratedLevel(new MarioLevelModel(maxLevelWidth, 16),initSeed, modifications);
		}
    }
}
