package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.FirstOrderIndexTuple;
import maxparser.parser.manager.Manager;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.iterator.TIntIntIterator;

public class FirstOrderNonProjDecoder extends Decoder{

	@SuppressWarnings("unchecked")
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		int length = inst.length();
		
		int[][] oldI = new int[length][length];
		int[][] oldO = new int[length][length];
		double[][] scoreMatrix = new double[length][length];
		double[][] orig_scoreMatrix = new double[length][length];
		boolean[] curr_nodes = new boolean[length];
		TIntHashSet[] reps = new TIntHashSet[length];
	
		manager.getTypes(length);
	
		FirstOrderIndexTuple index0 = new FirstOrderIndexTuple();
		FirstOrderIndexTuple index1 = new FirstOrderIndexTuple();
		
		for(int s = 0; s < length; ++s){
			curr_nodes[s] = true;
			reps[s] = new TIntHashSet();
			reps[s].add(s);
			for(int t = s + 1; t < length; ++t){
				//positive index
				index0.par = s;
				index0.ch = t;
				index0.type = manager.getType(index0);
				scoreMatrix[s][t] = manager.getScore(index0);
				orig_scoreMatrix[s][t] = scoreMatrix[s][t];
				oldI[s][t] = s;
				oldO[s][t] = t;
				
				//negative index
				index1.par = t;
				index1.ch = s;
				index1.type = manager.getType(index1);
				scoreMatrix[t][s] = manager.getScore(index1);
				orig_scoreMatrix[t][s] = scoreMatrix[t][s];
				oldI[t][s] = t;
				oldO[t][s] = s;
			}
		}
	
		TIntIntHashMap final_edges = new TIntIntHashMap();
		chuLiuEdmonds(scoreMatrix, curr_nodes, oldI, oldO, final_edges, reps, length);
		int[] par = new int[length];
		int[] types = new int[length];
		types[0] = 0;
	
		TIntIntIterator iter = final_edges.iterator();
		while(iter.hasNext()){
			int ch = iter.key();
			int pa = iter.value();
			par[ch] = pa;
			index0.ch = ch;
			index0.par = pa;
			types[ch] = manager.getType(index0);
			iter.advance();
		}
	
		int[] n_par = getKChanges(par, orig_scoreMatrix, (K - 1 < length) ? K - 1 : length, length);
	
		int new_k = 1;
		for(int np : n_par){
			if(np != -1){
				++new_k;
			}
		}
	
		Pair<FeatureVector, String>[] d = (Pair<FeatureVector, String>[]) new Pair[K];
		int nc = 0;
		for(int k = 0; k < K; ++k){
			d[k] = null;
			if(k == 0){
				d[k] = new Pair<FeatureVector, String>();
				d[k].first = getFeatureVector(inst, manager, model, par, types);
				d[k].second = manager.genTreeString(par, types);
			}
			else if(k < new_k){
				while(nc < length && n_par[nc] == -1){
					nc++;
				}
				int tmp_par = par[nc];
				int tmp_type = types[nc];
				par[nc] = n_par[nc];
				index0.ch = nc;
				index0.par = par[nc];
				types[nc] = manager.getType(index0);
				d[k].first = getFeatureVector(inst, manager, model, par, types);
				d[k].second = manager.genTreeString(par, types);
				par[nc] = tmp_par;
				types[nc] = tmp_type;
				++nc;
			}
		}
		return d;
	}
	
	private int[] getKChanges(int[] par, double[][] scoreMatrix, int K, int length) {
		int[] result = new int[length];
		
		return result;
	}
	
	private void chuLiuEdmonds(double[][] scoreMatrix, boolean[] curr_nodes, int[][] oldI, int[][] oldO, TIntIntHashMap final_edges, TIntHashSet[] reps, int length){
		// TODO
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
