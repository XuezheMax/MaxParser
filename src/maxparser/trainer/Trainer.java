package maxparser.trainer;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;

public abstract class Trainer {
	public abstract void updateParams(DependencyInstance inst, Pair<FeatureVector, String> d, double upd, ParserModel model);
	
	public abstract void updateParams(DependencyInstance inst, Pair<FeatureVector, String> d, ParserModel model);
}
