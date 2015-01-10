package maxparser.parser;

import maxparser.parser.decoder.SiblingProjDecoder;
import maxparser.parser.manager.SiblingManager;

public class SiblingProjParser extends SingleEdgeProjParser {
	
	public SiblingProjParser(){
		manager = new SiblingManager();
		decoder = new SiblingProjDecoder();
	}
}
