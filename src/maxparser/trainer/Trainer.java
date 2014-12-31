package maxparser.trainer;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public abstract class Trainer {
	public Trainer(){}
	
	public abstract void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, int numTrainInst, int numDevInst) throws TrainingException;
	
	public abstract void updateParams(DependencyInstance inst, Pair<FeatureVector, String>[] d, double upd, ParserModel model);
	
	public abstract void updateParams(DependencyInstance inst, Pair<FeatureVector, String>[] d, ParserModel model);
}
