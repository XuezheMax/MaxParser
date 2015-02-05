package maxparser.parser.marginal;

import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SiblingIndexTuple;

public class SiblingMarginal extends SingleEdgeMarginal{
	private double[][][] marginals = null;
	
	public SiblingMarginal(int length){
		marginals = new double[length][length][];
		for(int i = 0; i < length; ++i){
			for(int j = 0; j < length; ++j){
				int n = j < i ? j : length;
				marginals[i][j] = new double[n];
			}
		}
	}
	
	@Override
	public double getMarginal(IndexTuple itemId) {
		SiblingIndexTuple id = (SiblingIndexTuple) itemId;
		return marginals[id.par][id.ch1][id.ch];
	}
	
	@Override
	public int size() {
		return marginals.length;
	}
}
