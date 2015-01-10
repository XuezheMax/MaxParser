package maxparser.parser.decoder;

import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.DependencyInstance;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;

public abstract class Decoder {
	public Decoder(){}
	
	public abstract Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model);
	
	protected FeatureVector getFeatureVector(DependencyInstance inst, Manager manager, ParserModel model, int[] heads, int[] types) {
		int[] heads_tmp = inst.heads;
		int[] typeIds_tmp = inst.deprelIds;
		
		inst.heads = heads;
		inst.deprelIds = types;
		
		FeatureVector fv = manager.createFeatureVector(inst, model);
		
		inst.heads = heads_tmp;
		inst.deprelIds = typeIds_tmp;
		
		return fv;
	}
}
