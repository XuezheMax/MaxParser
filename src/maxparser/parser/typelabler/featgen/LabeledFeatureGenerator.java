package maxparser.parser.typelabler.featgen;

import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public abstract class LabeledFeatureGenerator {
	public LabeledFeatureGenerator(){}
	
	protected final void addFeature(String prefix, String feat, ParserModel model, FeatureVector fv){
		int pre = model.getPrefixIndex(prefix);
		int num = model.getFeatureIndex(pre + "=" + feat);
		if(num >= 0){
			fv.add(num, 1.0);
		}
	}
}
