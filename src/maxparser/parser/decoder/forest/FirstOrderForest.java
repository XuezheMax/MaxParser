package maxparser.parser.decoder.forest;

import maxparser.parser.decoder.forest.indextuple.FirstOrderForestIndexTuple;
import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;

public class FirstOrderForest extends Forest{
	protected ForestItem[][] chart = null;
	
	public FirstOrderForest(int end, int K){
		this.end = end;
		this.K = K;
		chart = new ForestItem[(end + 1) * (end + 1) * 2 * 2][K];
		
		for(short s = 0; s <= end; ++s){
			for(short t = 0; t <= end; ++t){
				for(short dir = 0; dir < 2; ++dir){
					for(short comp = 0; comp < 2; ++comp){
						for(short k = 0; k < K; ++k){
							chart[getKey(s, t, dir, comp)][k] = new ForestItem(s, (short) -1, t, (short) -1, dir, comp, Double.NEGATIVE_INFINITY, null, null);
						}
					}
				}
			}
		}
	}

	@Override
	protected int getKey(ForestIndexTuple forestIndex) {
		FirstOrderForestIndexTuple id = (FirstOrderForestIndexTuple) forestIndex;
		return getKey(id.s, id.t, id.dir, id.comp);
	}
	
	protected int getKey(short s, short t, short dir, short comp){
		int key = s;
		key = key * (end + 1) + t;
		key = key * 2 + dir;
		key = key * 2 + comp;
		return key;
	}

	@Override
	public ForestItem[] getItems(ForestIndexTuple forestIndex) {
		return chart[getKey(forestIndex)];
	}

	@Override
	public boolean addItem(ForestIndexTuple forestIndex, short type, double score, ForestItem left, ForestItem right) {
		boolean added = false;
		int key = getKey(forestIndex);
		if(chart[key][K - 1].score > score){
			return false;
		}
		
		FirstOrderForestIndexTuple id = (FirstOrderForestIndexTuple) forestIndex;
		for(int k = 0; k < K; ++k){
			if(chart[key][k].score < score){
				ForestItem tmp = chart[key][k];
				chart[key][k] = new ForestItem(id.s, id.r, id.t, type, id.dir, id.comp, score, left, right);
				for(int j = k + 1; j < K && tmp.score != Double.NEGATIVE_INFINITY; ++j){
					ForestItem tmp1 = chart[key][j];
					chart[key][j] = tmp;
					tmp = tmp1;
				}
				added = true;
				break;
			}
		}
		return added;
	}

}
