package maxparser.parser.marginal;

import maxparser.parser.indextuple.IndexTuple;

public abstract class Marginal {
	public abstract double getMarginal(IndexTuple itemId);
	
	public abstract int size();
}
