package maxparser.parser.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public abstract class FeatureGenerator {
	
	public abstract FeatureVector createFeatureVector(DependencyInstance instance, ParserModel model);
}
