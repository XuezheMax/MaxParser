package maxparser.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import maxparser.DependencyInstance;

public abstract class DependencyWriter {
	protected BufferedWriter writer;
	
	public static DependencyWriter createDependencyWriter(String readerClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (DependencyWriter) Class.forName(readerClassName).newInstance();
	}
	
	public void startWriting(String file) throws IOException {
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
	}
	
	public void close() throws IOException {
		writer.flush();
		writer.close();
	}
	
	public abstract void write(DependencyInstance instance, int[] heads, String[] types) throws IOException;
}
