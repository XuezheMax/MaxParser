package maxparser.parser;

import maxparser.exception.*;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.typelabler.TypeLabeler;
import maxparser.trainer.Trainer;

public abstract class Parser {
	protected ParserModel model = null;
	protected Manager manager = null;
	protected Decoder decoder = null;
	
	public Parser(){}
	
	public void train(Trainer trainer, TypeLabeler typeLabeler, ParserModel model, String trainfile, String devfile, String logfile, String modelfile) throws TrainingException {
		this.model = model;
		manager.setTypeLabeler(typeLabeler);
		int[] nums= manager.createInstance(trainfile, model.trainforest(), devfile, model.devforest(), model);
		manager.init(nums[0]);
		trainer.train(manager, decoder, model, trainfile, devfile, logfile, modelfile, nums[1], nums[2]);
	}
}
