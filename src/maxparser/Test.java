package maxparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		int[] keys = new int[5];
		keys[0] = 1;
		keys[1] = 2;
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("out.txt"));
		out.writeObject(keys);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("out.txt"));
		int[] kk = (int[]) in.readObject();
		in.close();
		System.out.println(kk[0] + " " + kk[1]);
	}

}
