package maxparser.parser;

import maxparser.parser.decoder.FirstOrderProjDecoder;
import maxparser.parser.manager.FirstOrderManager;

public class FirstOrderProjParser extends Parser{
	
	public FirstOrderProjParser(){
		manager = new FirstOrderManager();
		decoder  = new FirstOrderProjDecoder();
	}
}
