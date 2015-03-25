package maxparser.parser.decoder;

import java.io.IOException;

import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.DependencyInstance;
import maxparser.exception.TrainingException;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;
import maxparser.parser.marginal.Marginal;

public abstract class Decoder {
	public Decoder(){}
	
	public abstract Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model);
	
	public abstract double calcGradient(double[] gradient, double bound, Manager manager, ParserModel model, ObjectReader in1, ObjectReader in2) throws TrainingException, IOException, ClassNotFoundException;
	
	public abstract Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException;
	
	protected final void updateGradient(double[] gradient, int[] keys, double m){
		for(int i = 0; i < keys.length; ++i){
			gradient[keys[i]] += m;
		}
	}
	
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
