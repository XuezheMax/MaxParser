package maxparser.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ParserModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int default_form_size = 10000;
	private static final int default_pos_size = 100;
	private Parameters params = null;
	private ParserOptions options = null;
	private Alphabet featAlphabet = null;
	private Alphabet formAlphabet = null;
	private Alphabet lemmaAlphabet = null;
	private Alphabet cposAlphabet = null;
	private Alphabet posAlphabet = null;
	private Alphabet morphAlphabet = null;
	private Alphabet typeAlphabet = null;
	
	public ParserModel(ParserOptions options){
		this.options = options;
		featAlphabet = new Alphabet(options.map_size());
		formAlphabet = new Alphabet(default_form_size);
		lemmaAlphabet = new Alphabet(default_form_size);
		posAlphabet = new Alphabet(default_pos_size);
		cposAlphabet = new Alphabet(default_pos_size);
		morphAlphabet = new Alphabet(default_pos_size);
		typeAlphabet = new Alphabet(default_pos_size);
	}
	
	public void createParameters(int size){
		params = new Parameters(size);
	}
	
	//get feature index
	public int getFeatureIndex(String feat){
		return featAlphabet.lookupIndex(feat);
	}
	
	//get form index
	public int getFormIndex(String form){
		return formAlphabet.lookupIndex(form);
	}
	
	//get lemma index
	public int getLemmaIndex(String lemma){
		return lemmaAlphabet.lookupIndex(lemma);
	}
	
	//get cpos index
	public int getCPOSIndex(String cpos){
		return cposAlphabet.lookupIndex(cpos);
	}
	
	//get pos index
	public int getPOSIndex(String pos){
		return posAlphabet.lookupIndex(pos);
	}
	
	//get morph index
	public int getMorphIndex(String morph){
		return morphAlphabet.lookupIndex(morph);
	}
	
	//get type index
	public int getTypeIndex(String type){
		return typeAlphabet.lookupIndex(type);
	}
	
	public boolean isPunct(String pos){
		return options.isPunct(pos);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(options);
		out.writeObject(featAlphabet);
		out.writeObject(formAlphabet);
		out.writeObject(lemmaAlphabet);
		out.writeObject(cposAlphabet);
		out.writeObject(posAlphabet);
		out.writeObject(morphAlphabet);
		out.writeObject(typeAlphabet);
		out.writeObject(params);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		options = (ParserOptions) in.readObject();
		featAlphabet = (Alphabet) in.readObject();
		formAlphabet = (Alphabet) in.readObject();
		lemmaAlphabet = (Alphabet) in.readObject();
		cposAlphabet = (Alphabet) in.readObject();
		posAlphabet = (Alphabet) in.readObject();
		morphAlphabet = (Alphabet) in.readObject();
		typeAlphabet = (Alphabet) in.readObject();
		params = (Parameters) in.readObject();
	}
}
