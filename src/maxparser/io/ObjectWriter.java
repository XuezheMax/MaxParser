package maxparser.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ObjectWriter {
	private FileOutputStream fileOutStream = null;
	private ObjectOutputStream out = null;
	
	public ObjectWriter(String file){
		try {
			fileOutStream = new FileOutputStream(file);
			out = new ObjectOutputStream(fileOutStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void writeObject(Object obj) throws IOException{
		out.writeObject(obj);
	}
	
	public void writeInt(int val) throws IOException {
		out.writeInt(val);
	}
	
	public void writeBoolean(boolean val) throws IOException {
		out.writeBoolean(val);
	}
	
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void reset() throws IOException{
		out.reset();
	}
}
