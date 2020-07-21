package levelGenerators.juanCerrone;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class FileUtilities {

    //Guarda el archivo del nivel con las stats del mismo en su nombre
    public static void saveFile(String level,String folder){
        File generatedLevelsFolderFile = new File(folder);
        try {
            String filename = folder
                    + Objects.requireNonNull(generatedLevelsFolderFile.listFiles()).length
                    +  ".txt";
            FileWriter f = new FileWriter(filename);
            f.write(level);
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
