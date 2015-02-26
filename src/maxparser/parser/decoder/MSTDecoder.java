package maxparser.parser.decoder;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.manager.Manager;
import maxparser.parser.marginal.Marginal;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;

public class MSTDecoder extends Decoder{

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
	
		manager.getTypes(length, model);
	
		SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
		SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();
		
		for(int s = 0; s < length; ++s){
			curr_nodes[s] = true;
			reps[s] = new TIntHashSet();
			reps[s].add(s);
			for(int t = s + 1; t < length; ++t){
				//positive index
				index0.par = s;
				index0.ch = t;
				manager.getType(inst, index0, model);
				scoreMatrix[s][t] = manager.getScore(index0);
				orig_scoreMatrix[s][t] = scoreMatrix[s][t];
				oldI[s][t] = s;
				oldO[s][t] = t;
				
				//negative index
				index1.par = t;
				index1.ch = s;
				manager.getType(inst, index1, model);
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
			iter.advance();
			int ch = iter.key();
			int pa = iter.value();
			par[ch] = pa;
			index0.ch = ch;
			index0.par = pa;
			if(ch == 0){
				types[ch] = 0;
			}
			else{
				manager.getType(inst, index0, model);
				types[ch] = index0.type;
			}
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
				d[k] = new Pair<FeatureVector, String>();
				int tmp_par = par[nc];
				int tmp_type = types[nc];
				par[nc] = n_par[nc];
				index0.ch = nc;
				index0.par = par[nc];
				manager.getType(inst, index0, model);
				types[nc] = index0.type;
				d[k].first = getFeatureVector(inst, manager, model, par, types);
				d[k].second = manager.genTreeString(par, types);
				par[nc] = tmp_par;
				types[nc] = tmp_type;
				++nc;
			}
		}
		return d;
	}
	
	private boolean[][] calcChildren(int[] par, int length){
		boolean[][] isChild = new boolean[length][length];
		for(int i = 0; i < length; ++i){
			int pa = par[i];
			while(pa != -1){
				isChild[pa][i] = true;
				pa = par[pa];
			}
		}
		return isChild;
	}
	
	private int[] getKChanges(int[] par, double[][] scoreMatrix, int K, int length) {
		int[] result = new int[length];
		int[] n_par = new int[length];
		double[] n_score = new double[length];
		
		for(int i = 0; i < length; ++i){
			result[i] = -1;
			n_par[i] = -1;
			n_score[i] = Double.NEGATIVE_INFINITY;
		}
		
		boolean[][] isChild = calcChildren(par, length);
		
		for(int i = 1; i < length; ++i){
			double max = Double.NEGATIVE_INFINITY;
			int wh = -1;
			for(int j = 0; j < length; ++j){
				if(i == j || par[i] == j || isChild[i][j]){
					continue;
				}
				if(scoreMatrix[j][i] > max){
					max = scoreMatrix[j][i];
					wh = j;
				}
			}
			n_par[i] = wh;
			n_score[i] = max;
		}
		
		for(int k = 0; k < K; ++k){
			double max = Double.NEGATIVE_INFINITY;
			int wh = -1;
			int whI = -1;
			for(int i = 0; i < length; ++i){
				if(n_par[i] == -1){
					continue;
				}
				double diff = scoreMatrix[n_par[i]][i] - scoreMatrix[par[i]][i];
				if(diff > max){
					max = diff;
					whI = i;
					wh = n_par[i];
				}
			}
			if(whI == -1){
				break;
			}
			result[whI] = wh;
			n_par[whI] = -1;
		}
		return result;
	}
	
	private void chuLiuEdmonds(double[][] scoreMatrix, boolean[] curr_nodes, int[][] oldI, int[][] oldO, TIntIntHashMap final_edges, TIntHashSet[] reps, int length){
		// need to construct for each node list of nodes they represent (here only!)
		
		int[] par = new int[length];
		
		// create best graph
		par[0] = -1;
		for (int i = 1; i < length; i++){
			// only interested in current nodes
			if (curr_nodes[i]){
				double maxScore = scoreMatrix[0][i];
				par[i] = 0;
				for (int j = 1; j < length; j++){
					if (j == i || (!curr_nodes[j])){
						continue;
					}
					double newScore = scoreMatrix[j][i];
					if (newScore > maxScore){
						maxScore = newScore;
						par[i] = j;
					}
				}
			}
		}
	
		//find a cycle
		boolean[] added = new boolean[length];
		added[0] = true;
		TIntHashSet cycle = new TIntHashSet();
		boolean findCycle = false;
		for(int i = 1; i < length && !findCycle; ++i){
			if(added[i] || !curr_nodes[i]){
				continue;
			}
			
			//init cycle;
			TIntHashSet tmp_cycle = new TIntHashSet();
			tmp_cycle.add(i);
			added[i] = true;
			findCycle = true;
			int l = i;
			
			while(!tmp_cycle.contains(par[l])){
				l = par[l];
				if(added[l]){
					findCycle = false;
					break;
				}
				added[l] = true;
				tmp_cycle.add(l);
			}
			
			if(findCycle){
				int lorg = l;
				cycle.add(lorg);
				l = par[lorg];
				while(l != lorg){
					cycle.add(l);
					l = par[l];
				}
				break;
			}
		}
		
		//no cycles, get all edges and return them.
		if(!findCycle){
			final_edges.put(0, -1);
			for(int i = 1; i < length; ++i){
				if(!curr_nodes[i]){
					continue;
				}
				int pr = oldI[par[i]][i];
				int ch = oldO[par[i]][i];
				final_edges.put(ch, pr);
			}
			return;
		}
		
		int cyc_len = cycle.size();
		double cyc_weight = 0.0;
		int[] cyc_nodes = new int[cyc_len];
		TIntIterator iter = cycle.iterator();
		int id = 0;
		while(iter.hasNext()){
			int cyc_node = iter.next();
			cyc_nodes[id++] = cyc_node;
			cyc_weight += scoreMatrix[par[cyc_node]][cyc_node];
		}
		
		int rep = cyc_nodes[0];
		for(int i = 0; i < length; ++i){
			if(!curr_nodes[i] || cycle.contains(i)){
				continue;
			}
			
			double max1 = Double.NEGATIVE_INFINITY;
			int wh1 = -1;
			double max2 = Double.NEGATIVE_INFINITY;
			int wh2 = -1;

			for(int j = 0; j < cyc_len; ++j){
				int j1 = cyc_nodes[j];

				if(scoreMatrix[j1][i] > max1){
					max1 = scoreMatrix[j1][i];
					wh1 = j1;
				}

				double scr = cyc_weight + scoreMatrix[i][j1] - scoreMatrix[par[j1]][j1];

				if(scr > max2){
					max2 = scr;
					wh2 = j1;
				}
			}

			scoreMatrix[rep][i] = max1;
			oldI[rep][i] = oldI[wh1][i];
			oldO[rep][i] = oldO[wh1][i];
			scoreMatrix[i][rep] = max2;
			oldO[i][rep] = oldO[i][wh2];
			oldI[i][rep] = oldI[i][wh2];
		}
		
		TIntHashSet[] rep_cons = new TIntHashSet[cyc_len];
		for(int i = 0; i < cyc_len; ++i){
			int cyc_node = cyc_nodes[i];
			iter = reps[cyc_node].iterator();
			rep_cons[i] = new TIntHashSet();
			while(iter.hasNext()){
				rep_cons[i].add(iter.next());
			}
		}
		
		for(int i = 1; i < cyc_len; ++i){
			int cyc_node = cyc_nodes[i];
			curr_nodes[cyc_node] = false;
			iter = reps[cyc_node].iterator();
			while(iter.hasNext()){
				reps[rep].add(iter.next());
			}
		}
		
		chuLiuEdmonds(scoreMatrix, curr_nodes, oldI, oldO, final_edges, reps, length);
		
		// check each node in cycle, if one of its representatives
		// is a key in the final_edges, it is the one.
		boolean found = false;
		int wh = -1;
		for(int i = 0; i < cyc_len; ++i){
			iter = rep_cons[i].iterator();
			while(iter.hasNext()){
				if(final_edges.contains(iter.next())){
					wh = cyc_nodes[i];
					found = true;
					break;
				}
			}
			if(found){
				break;
			}
		}
		
		int l = par[wh];
		while (l != wh){
			int ch = oldO[par[l]][l];
			int pr = oldI[par[l]][l];
			final_edges.put(ch, pr);
			l = par[l];
		}
	}

	@Override
	public double calcGradient(double[] gradient, Manager manager, ParserModel model, ObjectReader in1, ObjectReader in2) throws TrainingException, IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
