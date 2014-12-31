package maxparser.parser.decoder;

import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.DependencyInstance;
import maxparser.parser.manager.Manager;

public abstract class Decoder {
	public Decoder(){}
	
	public abstract Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K);
	
}
