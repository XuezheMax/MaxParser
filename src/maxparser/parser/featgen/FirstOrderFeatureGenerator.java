package maxparser.parser.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class FirstOrderFeatureGenerator extends FeatureGenerator{
	
	public FirstOrderFeatureGenerator(){}
	
	@Override
	public FeatureVector createFeatureVector(DependencyInstance instance, ParserModel model){
		return null;
	}
}
