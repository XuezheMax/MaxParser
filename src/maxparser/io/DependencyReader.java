package maxparser.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import maxparser.DependencyInstance;
import maxparser.model.ParserModel;

public abstract class DependencyReader {
	
	protected BufferedReader inputReader;
	
	public static DependencyReader createDependencyReader(String readerClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (DependencyReader) Class.forName(readerClassName).newInstance();
	}
	
	public void startReading(String file) throws IOException{
		inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
	}
	
	public void close() throws IOException{
		inputReader.close();
	}
	
	public abstract DependencyInstance getNext(ParserModel model) throws IOException;
	
	protected String normalize(String s) {
		if (s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+")){
			return "<num>";
		}
		return s;
	}
}
