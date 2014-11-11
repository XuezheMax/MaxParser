package DependencyParser;

import DependencyDecoder.DependencyDecoder;
import DependencyPipe.DependencyPipe;
import ParserParameter.Parameters;
import ParserParameter.ParserOptions;


public abstract class DependencyParser {

	/**
	 * @param args
	 */
	public ParserOptions options;
	public DependencyPipe pipe;
	public Parameters params;
	
	private DependencyDecoder decoder;
	
	//To Do
	public DependencyParser(DependencyPipe pipe, ParserOptions options){
		this.pipe = pipe;
		this.options = options;
		
		params = new Parameters();
	}

	public abstract void train(int[] instanceLengths, String train_forest);
	
	protected abstract void trainingIter(int[] instanceLengths, int maxLength, String tran_forest, int iter);
	
}
