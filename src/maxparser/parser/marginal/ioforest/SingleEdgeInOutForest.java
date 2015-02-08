package maxparser.parser.marginal.ioforest;

import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;

public class SingleEdgeInOutForest extends InOutForest{
	
	protected double[][][][] beta = null;
	
	protected double[][][][] alpha = null;
	
	public SingleEdgeInOutForest(){
		beta = null;
		alpha = null;
	}
	
	public SingleEdgeInOutForest(int size){
		beta = new double[size][size][2][2];
		alpha = new double[size][size][2][2];
	}

	@Override
	public void addBeta(ForestIndexTuple index, double val) {
		BasicForestIndexTuple id = (BasicForestIndexTuple) index;
		beta[id.s][id.t][id.dir][id.comp] = val;
	}

	@Override
	public void addAlpha(ForestIndexTuple index, double val) {
		BasicForestIndexTuple id = (BasicForestIndexTuple) index;
		alpha[id.s][id.t][id.dir][id.comp] = val;
	}

	@Override
	public double getBeta(ForestIndexTuple index) {
		BasicForestIndexTuple id = (BasicForestIndexTuple) index;
		return beta[id.s][id.t][id.dir][id.comp];
	}

	@Override
	public double getAlpha(ForestIndexTuple index) {
		BasicForestIndexTuple id = (BasicForestIndexTuple) index;
		return alpha[id.s][id.t][id.dir][id.comp];
	}

}
