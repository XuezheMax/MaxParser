package maxparser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.iterator.TIntIntIterator;

public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		TIntIntHashMap map = new TIntIntHashMap();
		map.put(0, 1);
		map.put(1, 2);
		TIntIntIterator iter = map.iterator();
		while(iter.hasNext()){
			iter.advance();
			System.out.println(iter.key() + " " + iter.value());
		}
	}

}
