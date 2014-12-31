package maxparser.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import maxparser.FeatureVector;

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
	private String[] types = null;
	
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
	
	private ParserModel(Parameters params){
		this.params = params;
	}
	
	public void createParameters(int size){
		params = new Parameters(size);
	}
	
	public void closeAlphabets(){
		featAlphabet.stopGrowth();
		formAlphabet.stopGrowth();
		lemmaAlphabet.stopGrowth();
		cposAlphabet.stopGrowth();
		posAlphabet.stopGrowth();
		morphAlphabet.stopGrowth();
		typeAlphabet.stopGrowth();
		
		types = new String[typeAlphabet.size()];
		String[] keys = typeAlphabet.toArray();
		for (String key : keys) {
			int indx = typeAlphabet.lookupIndex(key);
			types[indx] = key;
		}
	}
	
	public double getScore(FeatureVector fv){
		return params.getScore(fv);
	}
	
	public void update(FeatureVector fv, double alpha_k, double upd){
		params.update(fv, alpha_k, upd);
	}
	
	public void update(FeatureVector fv, double alpha_k){
		params.update(fv, alpha_k);
	}
	
	public void updateTotal(){
		params.updateTotal();
	}
	
	public ParserModel getTemporalModel(double avVal){
		Parameters tempParams = params.getTemporalParames(avVal);
		return new ParserModel(tempParams);
	}
	
	public void averageParams(double avVal){
		params.averageParams(avVal);
	}
	
	//get feature index
	public int getFeatureIndex(String feat){
		return featAlphabet.lookupIndex(feat);
	}
	
	//get feature size
	public int featureSize(){
		return featAlphabet.size();
	}
	
	//get form index
	public int getFormIndex(String form){
		return formAlphabet.lookupIndex(form);
	}
	
	//get form size
	public int formSize(){
		return formAlphabet.size();
	}
	
	//get lemma index
	public int getLemmaIndex(String lemma){
		return lemmaAlphabet.lookupIndex(lemma);
	}
	
	//get lemma size
	public int lemmaSize(){
		return lemmaAlphabet.size();
	}
	
	//get cpos index
	public int getCPOSIndex(String cpos){
		return cposAlphabet.lookupIndex(cpos);
	}
	
	//get cpos size
	public int cposSize(){
		return cposAlphabet.size();
	}
	
	//get pos index
	public int getPOSIndex(String pos){
		return posAlphabet.lookupIndex(pos);
	}
	
	//get pos size
	public int posSize(){
		return posAlphabet.size();
	}
	
	//get morph index
	public int getMorphIndex(String morph){
		return morphAlphabet.lookupIndex(morph);
	}
	
	//get type index
	public int getTypeIndex(String type){
		return typeAlphabet.lookupIndex(type);
	}
	
	//get type string via index
	public String getType(int index){
		return types[index];
	}
	
	//get type size
	public int typeSize(){
		return typeAlphabet.size();
	}
	
	public boolean isPunct(String pos){
		return options.isPunct(pos);
	}
	
	public boolean labeled(){
		return options.labeled();
	}
	
	public String lossType(){
		return options.lossType();
	}
	
	public boolean createforest(){
		return options.createforest();
	}
	
	public String trainforest(){
		return options.getTrainForest();
	}
	
	public String devforest(){
		return options.getDevForest();
	}
	
	public String getReader(){
		return options.getReader();
	}
	
	public String getWriter(){
		return options.getWriter();
	}
	
	public int threadNum(){
		return options.getThreadNum();
	}
	
	public int iterNum(){
		return options.getIterNum();
	}
	
	public int trainingK(){
		return options.getTrainingK();
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
