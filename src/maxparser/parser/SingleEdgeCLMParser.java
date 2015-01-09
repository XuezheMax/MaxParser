package maxparser.parser;

import maxparser.parser.decoder.SingleEdgeCLMDecoder;
import maxparser.parser.manager.SingleEdgeManager;

public class SingleEdgeCLMParser extends SingleEdgeProjParser{
	public SingleEdgeCLMParser(){
		manager = new SingleEdgeManager();
		decoder = new SingleEdgeCLMDecoder();
	}
}
