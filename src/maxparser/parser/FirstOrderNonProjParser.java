package maxparser.parser;

import maxparser.parser.decoder.FirstOrderNonProjDecoder;
import maxparser.parser.manager.FirstOrderManager;

public class FirstOrderNonProjParser extends Parser{
	public FirstOrderNonProjParser(){
		manager = new FirstOrderManager();
		decoder  = new FirstOrderNonProjDecoder();
	}
}
