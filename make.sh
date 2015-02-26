javac -classpath ".:lib/trove.jar" -sourcepath src/ src/maxparser/MaxParser.java -d ./
jar cfm MaxParser.jar lib/manifest.mf maxparser/ lib/trove.jar
rm -f tmp/
mkdir tmp/
