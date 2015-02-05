package maxparser.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectReader {
	private FileInputStream fileInStream = null;
	private ObjectInputStream in = null;
	
	public ObjectReader(String file){
		try {
			fileInStream = new FileInputStream(file);
			in = new ObjectInputStream(fileInStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public Object readObject() throws ClassNotFoundException, IOException{
		return in.readObject();
	}
	
	public int readInt() throws IOException {
		return in.readInt();
	}
	
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}
	
	public long tell() throws IOException {
		return fileInStream.getChannel().position();
	}
	
	public void seek(long position) throws IOException {
		fileInStream.getChannel().position(position);
	}
	
	public void close(){
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
