package maxparser.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectIO {
	public static ObjectInputStream getObjectInputStream(String file){
		try {
			return new ObjectInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.exit(0);
				return null;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}
	
	public static void close(ObjectInputStream in){
		if(in == null){
			return;
		}
		
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static ObjectOutputStream getObjectOutputStream(String file){
		try {
			return new ObjectOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
	}
	
	public static void close(ObjectOutputStream out){
		if(out == null){
			return;
		}
		
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
