package maxparser.model;

import java.io.Serializable;
import java.util.HashSet;

public class ParserOptions implements Serializable{
	/**
	 * 
	 */
	private HashSet<String> punctSet;
	private static final long serialVersionUID = 1L;
	private boolean labeled = false;
	private int map_size = 100000;
	private String lossType = "nopunc";
	
	public boolean labeled(){
		return labeled;
	}
	
	public int map_size(){
		return map_size;
	}
	
	public String lossType(){
		return lossType;
	}
	
	boolean isPunct(String pos){
		return punctSet.contains(pos);
	}
}
