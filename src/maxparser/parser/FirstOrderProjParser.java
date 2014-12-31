package maxparser.parser;

import maxparser.exception.TrainingException;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.FirstOrderProjDecoder;
import maxparser.parser.manager.FirstOrderManager;
import maxparser.parser.typelabler.TypeLabeler;
import maxparser.trainer.Trainer;

public class FirstOrderProjParser extends Parser{
	
	public FirstOrderProjParser(){
		manager = new FirstOrderManager();
		decoder  = new FirstOrderProjDecoder();
	}
}
