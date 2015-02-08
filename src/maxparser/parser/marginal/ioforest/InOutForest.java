package maxparser.parser.marginal.ioforest;
import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;

public abstract class InOutForest {
	public InOutForest(){}
	
	public InOutForest(int size){}
	
	public abstract void addBeta(ForestIndexTuple index, double val);
	
	public abstract void addAlpha(ForestIndexTuple index, double val);
	
	public abstract double getBeta(ForestIndexTuple index);
	
	public abstract double getAlpha(ForestIndexTuple index);
}
