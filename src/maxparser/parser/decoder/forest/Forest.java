package maxparser.parser.decoder.forest;

import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;

public abstract class Forest {
	protected int end, K;
	
	protected abstract int getKey(ForestIndexTuple forestIndex);
	
	public abstract ForestItem[] getItems(ForestIndexTuple forestIndex);
	
	public abstract boolean addItem(ForestIndexTuple forestIndex, short type, double score, ForestItem left, ForestItem right);
}
