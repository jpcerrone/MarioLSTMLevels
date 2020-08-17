# Mario LSTM Levels
Un generador de niveles para Super Mario Bros, basado en la utilización de redes LSTM.

# Set Up
Se deben instalar las dependencias del proyecto especificadas en pom.xml utilizando Maven. En el caso de que se quiera entrenar nuevamente a la red se deberá configurar el tipo de backend (GPU/CPU) en el archivo de pom.xml. Más información: https://deeplearning4j.konduit.ai/config/backends

# Ejecución
El generador puede ser utilizado ejecutando https://github.com/jpcerrone/MarioLSTMLevels/blob/master/src/main/java/GenerateLevel.java . En este archivo pueden además especificarse distintos parámetros para la generación, como cantidad de niveles, longitud máxima, tipo de los niveles y modificaciónes de bloques.

Los niveles generados son guardados en la carpeta levels/jC , pero además puede ser visualizados utilizando la herramienta Visualizador.jar.

Se puede jugar a los niveles especificando el archivo del nivel en https://github.com/jpcerrone/MarioLSTMLevels/blob/master/src/main/java/PlayLevel.java. También se puede hacer que lo jueguen los distntos agentes inteligentes.

# Referencias
El código del proyecto está escrito sobre MarioAI Framework: https://github.com/amidos2006/Mario-AI-Framework.

El trabajo final asociado a este programa se puede encontrar en: **agregar link**
