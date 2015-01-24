package maxparser.parser.typelabler.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class BasicLabeledFeatureGenerator extends LabeledFeatureGenerator{

	public BasicLabeledFeatureGenerator(){}
	
	public void addChildSingleLabelFeatures(DependencyInstance inst, int ch, int type, boolean attR, ParserModel model, FeatureVector fv){
		
	}
	
	public void addParentSingleLabelFeatures(DependencyInstance inst, int ch, int type, boolean attR, ParserModel model, FeatureVector fv){
		
	}

	public void addSingleEdgeLabeledFeatures(DependencyInstance inst, int par, int ch, ParserModel model, FeatureVector fv){
		
	}
	
	public void addTwoObsLabeledFeatures(String prefix, int type, int obs1F1, int obs1F2, int obs2F1, int obs2F2, String attachDistance, ParserModel model, FeatureVector fv){
		
	}
}
