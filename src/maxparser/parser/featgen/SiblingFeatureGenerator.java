package maxparser.parser.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class SiblingFeatureGenerator extends SingleEdgeFeatureGenerator{
	
	@Override
	public void genUnlabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv) {
		super.genUnlabeledFeatures(inst, model, fv);
		int length = inst.length();
		
		// find all trip features
		for(int i = 0; i < length; ++i){
			if(inst.heads[i] == -1 && i != 0){
				continue;
			}
			
			// right children
			int prev = i;
			for(int j = i + 1; j < length; ++j){
				if(inst.heads[j] == i){
					addTripFeatures(inst, i, prev, j, model, fv);
					addSiblingFeatures(inst, prev, j, prev == i, model, fv);
					prev = j;
				}
			}
			
			//left children
			for(int j = i - 1; j >= 0; --j){
				if(inst.heads[j] == i){
					addTripFeatures(inst, i, prev, j, model, fv);
					addSiblingFeatures(inst, prev, j, prev == i, model, fv);
					prev = j;
				}
			}
		}
	}
	
	public void addSiblingFeatures(DependencyInstance inst, int ch1, int ch2, boolean isST, ParserModel model, FeatureVector fv){
		
	}
	
	public void addTripFeatures(DependencyInstance inst, int par, int ch1, int ch2, ParserModel model, FeatureVector fv){
		
	}
}
