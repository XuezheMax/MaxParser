package maxparser.parser;

import maxparser.parser.decoder.MSTDecoder;
import maxparser.parser.manager.SingleEdgeManager;

public class MSTParser extends Parser{
	public MSTParser(){
		manager = new SingleEdgeManager();
		decoder  = new MSTDecoder();
	}
}
