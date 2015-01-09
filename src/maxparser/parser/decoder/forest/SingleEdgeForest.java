package maxparser.parser.decoder.forest;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.indextuple.SingleEdgeForestIndexTuple;
import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;
import maxparser.parser.manager.Manager;

public class SingleEdgeForest extends Forest{
	protected ForestItem[][] chart = null;
	
	public SingleEdgeForest(int end, int K){
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
		SingleEdgeForestIndexTuple id = (SingleEdgeForestIndexTuple) forestIndex;
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
	public boolean addItem(ForestIndexTuple forestIndex, short r, short type, double score, ForestItem left, ForestItem right) {
		boolean added = false;
		int key = getKey(forestIndex);
		if(chart[key][K - 1].score > score){
			return false;
		}
		
		SingleEdgeForestIndexTuple id = (SingleEdgeForestIndexTuple) forestIndex;
		for(int k = 0; k < K; ++k){
			if(chart[key][k].score < score){
				ForestItem tmp = chart[key][k];
				chart[key][k] = new ForestItem(id.s, r, id.t, type, id.dir, id.comp, score, left, right);
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

	@SuppressWarnings("unchecked")
	@Override
	public Pair<FeatureVector, String>[] getBestParses(DependencyInstance inst, Manager manager, ParserModel model) {
		Pair<FeatureVector, String>[] d = (Pair<FeatureVector, String>[]) new Pair[K];
		int key = getKey((short) 0, (short) end, (short) 0, (short) 1);
		for(int k = 0; k < K; ++k){
			d[k] = null;
			if(chart[key][k].score != Double.NEGATIVE_INFINITY){
				d[k] = new Pair<FeatureVector, String>();
				d[k].second = getDepString(chart[key][k]);
				d[k].first = getFeatureVector(inst, manager, model, d[k].second);
			}
		}
		return d;
	}

	@Override
	protected String getDepString(ForestItem item) {
		if(item.left == null){
			return "";
		}
		
		if(item.comp == 1){
			return (getDepString(item.left) + " " + getDepString(item.right)).trim();
		}
		else if(item.dir == 0){
			return ((getDepString(item.left) + " " + getDepString(item.right)).trim() 
					+ " " + item.s + "|" + item.t + ":" + item.type).trim();
		}
		else{
			return (item.t + "|" + item.s + ":" + item.type + " " 
					+ (getDepString(item.left) + " " + getDepString(item.right)).trim()).trim();
		}
	}

}
