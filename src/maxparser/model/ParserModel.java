package maxparser.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

import maxparser.FeatureVector;
import maxparser.trainer.lbfgs.LBFGS.ExceptionWithIflag;
import maxparser.trainer.lbfgs.SimpleLBFGS;

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
	private Alphabet prefixAlphabet = null;
	
	public ParserModel(ParserOptions options){
		this.options = options;
		featAlphabet = new Alphabet(options.map_size());
		formAlphabet = new Alphabet(default_form_size);
		lemmaAlphabet = new Alphabet(default_form_size);
		posAlphabet = new Alphabet(default_pos_size);
		cposAlphabet = new Alphabet(default_pos_size);
		morphAlphabet = new Alphabet(default_pos_size);
		typeAlphabet = new Alphabet(default_pos_size);
		prefixAlphabet = new Alphabet(default_pos_size);
	}
	
	private ParserModel(Parameters params, ParserOptions options, Alphabet featAlphabet, 
			Alphabet formAlphabet, Alphabet lemmaAlphabet, 
			Alphabet cposAlphabet, Alphabet posAlphabet, 
			Alphabet morphAlphabet, Alphabet typeAlphabet,
			String[] types, Alphabet prefixAlphabet){
		this.params = params;
		this.options = options;
		this.featAlphabet = featAlphabet;
		this.formAlphabet = formAlphabet;
		this.lemmaAlphabet = lemmaAlphabet;
		this.cposAlphabet = cposAlphabet;
		this.posAlphabet = posAlphabet;
		this.morphAlphabet = morphAlphabet;
		this.typeAlphabet = typeAlphabet;
		this.types = types;
		this.prefixAlphabet = prefixAlphabet;
	}
	
	public void createParameters(){
		params = new Parameters(featAlphabet.size());
	}
	
	public void closeAlphabets(){
		featAlphabet.stopGrowth();
		formAlphabet.stopGrowth();
		lemmaAlphabet.stopGrowth();
		cposAlphabet.stopGrowth();
		posAlphabet.stopGrowth();
		morphAlphabet.stopGrowth();
		typeAlphabet.stopGrowth();
		prefixAlphabet.stopGrowth();
		
		types = new String[typeAlphabet.size()];
		Object[] keys = typeAlphabet.toArray();
		for (Object key : keys) {
			int indx = typeAlphabet.lookupIndex((String) key);
			types[indx] = (String) key;
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
	
	public int update(SimpleLBFGS lbfgs, double f, double[] g) throws ExceptionWithIflag{
		return params.update(lbfgs, f, g);
	}
	
	public ParserModel getTemporalModel(double avVal){
		Parameters tempParams = params.getTemporalParames(avVal);
		return new ParserModel(tempParams, this.options, this.featAlphabet,
				this.formAlphabet, this.lemmaAlphabet, this.cposAlphabet, this.posAlphabet,
				this.morphAlphabet, this.typeAlphabet, this.types, this.prefixAlphabet);
	}
	
	public void averageParams(double avVal){
		params.averageParams(avVal);
	}
	
	public double paramAt(int index){
		return params.paramAt(index);
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
	
	//get prefix index
	public int getPrefixIndex(String prefix){
		return prefixAlphabet.lookupIndex(prefix);
	}
	
	public boolean isPunct(String pos){
		return options.isPunct(pos);
	}
	
	public boolean labeled(){
		return options.labeled();
	}
	
	public boolean nopunc(){
		return options.lossType().equals("nopunc");
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
	
	public String getParser(){
		return options.getParser();
	}
	
	public String getTrainer(){
		return options.getTrainer();
	}
	
	public String getTypeLabeler(){
		return options.getTypeLabeler();
	}
	
	public int threadNum(){
		return options.getThreadNum();
	}
	
	public double cost(){
		return options.getCost();
	}
	
	public int iterNum(){
		return options.getIterNum();
	}
	
	public int maxIter(){
		return options.maxIter();
	}
	
	public int trainingK(){
		return options.getTrainingK();
	}
	
	public void putReader(String reader){
		options.putReader(reader);
	}
	
	public void putWriter(String writer){
		options.putWriter(writer);
	}
	
	public void setPunctSet(HashSet<String> punctSet){
		options.setPunctSet(punctSet);
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
		out.writeObject(prefixAlphabet);
		out.writeObject(params);
		out.writeObject(types);
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
		prefixAlphabet = (Alphabet) in.readObject();
		params = (Parameters) in.readObject();
		types = (String[]) in.readObject();
	}
}
