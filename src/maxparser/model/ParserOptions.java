package maxparser.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import maxparser.exception.OptionException;

public class ParserOptions implements Serializable{
	/**
	 * 
	 */
	private final static String PARSER = "parser",
								DEFAULT_PARSER_CLASS = maxparser.parser.SingleEdgeProjParser.class.getName(),
								TRAINER = "trainer",
								DEFAULT_TRAINER_CLASS = maxparser.trainer.MIRAMLTrainer.class.getName(),
								TYPE_LABELER = "typelabeler",
								DEFAULT_TYPELABELER_CLASS = maxparser.parser.typelabler.DefaultTypeLabeler.class.getName(),
								SENT_READER = "reader",
								DEFAULT_SENT_READER_CLASS = maxparser.io.CONLLReader.class.getName(),
								SENT_WRITER = "writer",
								DEFAULT_SENT_WRITER_CLASS = maxparser.io.CONLLWriter.class.getName(),
								MODE = "mode",
								DEFAULT_MODE = "test",
								LOSS_TYPE = "loss-type",
								DEFAULT_LOSS_TYPE = "nopunc",
								THREAD_NUM = "thread-num",
								DEFAULT_THREAD_NUM = "1",
								PUNCTUATION = "punct",
								DEFAULT_PUNCTUATION = "",
								COST = "cost",
								DEFAULT_COST = "1.0",
								MAP_SIZE = "map-size",
								DEFAULT_MAP_SIZE = "1000000",
								ITER_NUMBER = "iters",
								DEFAULT_ITER_NUMBER = "10",
								TRAININGK = "training-k",
								DEFAULT_TRAININGK = "1",
								CREATE_FOREST = "create-forest",
								DEFAULT_CREATE_FOREST = Boolean.FALSE.toString(),
								CONFIGURATION = "config",
								TRAININGFILE = "train-file",
								TESTFILE = "test-file",
								DEVFILE = "dev-file",
								OUTFILE = "output-file",
								GOLDFILE = "gold-file",
								LOGFILE = "log-file",
								MODELFILE = "model-file";
	
	private gnu.trove.map.hash.THashMap<String, String> argToValueMap = null;
	private HashSet<String> valid_opt_set = null;
	private HashSet<String> punctSet = null;
	private static final long serialVersionUID = 1L;
	private final int maxiter = 5000;
	private final double stop_eta = 0.000001;
	private boolean labeled;
	private final String train_forest = "tmp/train.forest";
	private final String dev_forest = "tmp/dev.forest";
	
	private void init(){
		argToValueMap = new gnu.trove.map.hash.THashMap<String, String>();
		valid_opt_set = new HashSet<String>();
		punctSet = new HashSet<String>(0);
		//parser
		valid_opt_set.add(PARSER);
		argToValueMap.put(PARSER, DEFAULT_PARSER_CLASS);
		//trainer
		valid_opt_set.add(TRAINER);
		argToValueMap.put(TRAINER, DEFAULT_TRAINER_CLASS);
		//type labeler
		valid_opt_set.add(TYPE_LABELER);
		argToValueMap.put(TYPE_LABELER, DEFAULT_TYPELABELER_CLASS);
		//reader
		valid_opt_set.add(SENT_READER);
		argToValueMap.put(SENT_READER, DEFAULT_SENT_READER_CLASS);
		//writer
		valid_opt_set.add(SENT_WRITER);
		argToValueMap.put(SENT_WRITER, DEFAULT_SENT_WRITER_CLASS);
		//mode
		valid_opt_set.add(MODE);
		argToValueMap.put(MODE, DEFAULT_MODE);
		//loss type
		valid_opt_set.add(LOSS_TYPE);
		argToValueMap.put(LOSS_TYPE, DEFAULT_LOSS_TYPE);
		//thread num
		valid_opt_set.add(THREAD_NUM);
		argToValueMap.put(THREAD_NUM, DEFAULT_THREAD_NUM);
		//punctuation
		valid_opt_set.add(PUNCTUATION);
		argToValueMap.put(PUNCTUATION, DEFAULT_PUNCTUATION);
		//cost
		valid_opt_set.add(COST);
		argToValueMap.put(COST, DEFAULT_COST);
		//map size
		valid_opt_set.add(MAP_SIZE);
		argToValueMap.put(MAP_SIZE, DEFAULT_MAP_SIZE);
		//iter number
		valid_opt_set.add(ITER_NUMBER);
		argToValueMap.put(ITER_NUMBER, DEFAULT_ITER_NUMBER);
		//training k
		valid_opt_set.add(TRAININGK);
		argToValueMap.put(TRAININGK, DEFAULT_TRAININGK);
		//create forest
		valid_opt_set.add(CREATE_FOREST);
		argToValueMap.put(CREATE_FOREST, DEFAULT_CREATE_FOREST);
		//file name
		valid_opt_set.add(TRAININGFILE);
		valid_opt_set.add(TESTFILE);
		valid_opt_set.add(DEVFILE);
		valid_opt_set.add(OUTFILE);
		valid_opt_set.add(GOLDFILE);
		valid_opt_set.add(MODELFILE);
		valid_opt_set.add(LOGFILE);
		valid_opt_set.add(CONFIGURATION);
	}
	
	private String helpInfo(){
		// TODO
		return "Usage:\n";
	}
	
	private void parseOptions(String[] args) throws OptionException{
		for(int i = 0; i < args.length; i+=2) {
			String argumentIdentifier = args[i];
			if(!argumentIdentifier.startsWith("-") || argumentIdentifier.length() <= 1) {
				throw new OptionException("unexpected argument name: " + argumentIdentifier);
			}
			else{
				String argIdName = argumentIdentifier.substring(1);
				if(!valid_opt_set.contains(argIdName)){
					throw new OptionException("unexpected argument name: " + argumentIdentifier + "\n" + helpInfo());
				}
				else{
					String argValue = args[i + 1];
					argToValueMap.put(argIdName, argValue);
				}
			}
		}
		String configfile = getArgValue(CONFIGURATION);
		if(configfile != null){
			parseOptions(configfile);
		}
		
		labeled = !getArgValue(TYPE_LABELER).equals(DEFAULT_TYPELABELER_CLASS);
		String[] tokens = getArgValue(PUNCTUATION).split("\\|");
		for(String punc : tokens){
			punctSet.add(punc);
		}
		checkError();
	}
	
	private void parseOptions(String configfile) throws OptionException{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configfile)));
			String line = reader.readLine();
			while(line != null){
				line = line.trim();
				if(line.length() == 0){
					line = reader.readLine().trim();
					continue;
				}
				String[] tokens = line.split("=");
				if(!valid_opt_set.contains(tokens[0])){
					reader.close();
					throw new OptionException("unexpected argument name: " + tokens[0] + "\n" + helpInfo());
				}
				else{
					argToValueMap.put(tokens[0], tokens[1]);
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			throw new OptionException(e.getMessage());
		}
	}
	
	private void checkError() throws OptionException{
		// TODO
		String mode = getArgValue(MODE);
		if(mode.equals("test")){
			if(getArgValue(TESTFILE) == null){
				throw new OptionException("no test file\n" + helpInfo());
			}
			if(getArgValue(MODELFILE) == null){
				throw new OptionException("no model file\n" + helpInfo());
			}
			if(getArgValue(OUTFILE) == null){
				throw new OptionException("no output file\n" + helpInfo());
			}
		}
		else if(mode.equals("eval")){
			if(getArgValue(OUTFILE) == null){
				throw new OptionException("no output file\n" + helpInfo());
			}
			if(getArgValue(GOLDFILE) == null){
				throw new OptionException("no gold standard file\n" + helpInfo());
			}
		}
	}
	
	public ParserOptions(String[] args){
		init();
		try {
			parseOptions(args);
		} catch (OptionException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private String getArgValue(String argName){
		return argToValueMap.get(argName);
	}
	
	private void putArgValue(String argName, String value){
		argToValueMap.put(argName, value);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(getParser());
		out.writeObject(getTypeLabeler());
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		String parser = (String) in.readObject();
		String typeLabeler = (String) in.readObject();
		punctSet = new HashSet<String>(0);
		argToValueMap = new gnu.trove.map.hash.THashMap<String, String>();
		argToValueMap.put(PARSER, parser);
		argToValueMap.put(TYPE_LABELER, typeLabeler);
		labeled = !typeLabeler.equals(DEFAULT_TYPELABELER_CLASS);
	}
	
	public String getReader(){
		return getArgValue(SENT_READER);
	}
	
	public String getWriter(){
		return getArgValue(SENT_WRITER);
	}
	
	public String getParser(){
		return getArgValue(PARSER);
	}
	
	public String getTrainer(){
		return getArgValue(TRAINER);
	}
	
	public String getTypeLabeler(){
		return getArgValue(TYPE_LABELER);
	}
	
	public String getMode(){
		return getArgValue(MODE);
	}
	
	public int getThreadNum(){
		return Integer.parseInt(getArgValue(THREAD_NUM));
	}
	
	public double getCost(){
		return Double.parseDouble(getArgValue(COST));
	}

	public boolean labeled(){
		return labeled;
	}
	
	public int map_size(){
		return Integer.parseInt(getArgValue(MAP_SIZE));
	}
	
	public String lossType(){
		return getArgValue(LOSS_TYPE);
	}
	
	public boolean isPunct(String pos){
		return punctSet.contains(pos);
	}
	
	public int getIterNum(){
		return Integer.parseInt(getArgValue(ITER_NUMBER));
	}
	
	public int getTrainingK(){
		return Integer.parseInt(getArgValue(TRAININGK));
	}
	
	public boolean createforest(){
		return Boolean.parseBoolean(getArgValue(CREATE_FOREST));
	}
	
	public int maxIter(){
		return maxiter;
	}
	
	public double stopEta(){
		return stop_eta;
	}
	
	public String getTrainingFile(){
		return getArgValue(TRAININGFILE);
	}
	
	public String getTestFile(){
		return getArgValue(TESTFILE);
	}
	
	public String getDevFile(){
		return getArgValue(DEVFILE);
	}
	
	public String getOutFile(){
		return getArgValue(OUTFILE);
	}
	
	public String getGoldFile(){
		return getArgValue(GOLDFILE);
	}
	
	public String getLogFile(){
		return getArgValue(LOGFILE);
	}
	
	public String getModelFile(){
		return getArgValue(MODELFILE);
	}
	
	public String getTrainForest(){
		return train_forest;
	}
	
	public String getDevForest(){
		return dev_forest;
	}
	
	public void putReader(String reader){
		putArgValue(SENT_READER, reader);
	}
	
	public void putWriter(String writer){
		putArgValue(SENT_WRITER, writer);
	}
	
	public HashSet<String> getPunctSet(){
		return this.punctSet;
	}
	
	public void setPunctSet(HashSet<String> punctSet){
		this.punctSet = punctSet;
	}
}
