package maxparser.parser;

import maxparser.parser.decoder.SingleEdgeProjDecoder;
import maxparser.parser.manager.SingleEdgeManager;

public class SingleEdgeProjParser extends Parser{
	
	public SingleEdgeProjParser(){
		manager = new SingleEdgeManager();
		decoder  = new SingleEdgeProjDecoder();
	}
}
