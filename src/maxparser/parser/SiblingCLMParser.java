package maxparser.parser;

import maxparser.parser.decoder.SiblingCLMDecoder;
import maxparser.parser.manager.SiblingManager;

public class SiblingCLMParser extends SingleEdgeCLMParser{
	public SiblingCLMParser(){
		manager = new SiblingManager();
		decoder = new SiblingCLMDecoder();
	}
}
