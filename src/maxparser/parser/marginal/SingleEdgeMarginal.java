package maxparser.parser.marginal;

import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;

public class SingleEdgeMarginal extends Marginal{
	private double[][] marginals = null;
	
	public SingleEdgeMarginal(){
		marginals = null;
	}
	
	public SingleEdgeMarginal(int length){
		marginals = new double[length][length];
	}
	
	@Override
	public double getMarginal(IndexTuple itemId) {
		SingleEdgeIndexTuple id = (SingleEdgeIndexTuple) itemId;
		return marginals[id.par][id.ch];
	}

	@Override
	public int size() {
		return marginals.length;
	}

}
