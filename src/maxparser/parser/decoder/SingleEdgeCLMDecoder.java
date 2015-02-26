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

public class SingleEdgeCLMDecoder extends SingleEdgeProjDecoder{
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		Pair<FeatureVector, String>[] d = super.decode(manager, inst, K, model);
		int length = inst.length();
		int new_k = 0;
		for(int i = 0; i < d.length && d[i] != null; ++i){
			new_k = i + 1;
		}
		
		int[] par = null;
		int[] types = null;
		
		for(int i = 0; i < new_k; ++i){
			Pair<int[], int[]> pair = manager.getHeadsTypesfromTreeString(d[i].second);
			par = pair.first;
			types = pair.second;
			
			rearrange(inst, manager, par, types, length, model);
			
			d[i].first = getFeatureVector(inst, manager, model, par, types);
			d[i].second = manager.genTreeString(par, types);
		}
		
		return d;
	}
	
	protected boolean[][] calcChildren(int[] par, int length){
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
	
	protected void rearrange(DependencyInstance inst, Manager manager, int[] par, int[] types, int length, ParserModel model){
		boolean[][] isChild = calcChildren(par, length);
		SingleEdgeIndexTuple index = new SingleEdgeIndexTuple();
		while(true){
			int wh = -1, nPar = -1, nType = -1;
			double max = Double.NEGATIVE_INFINITY;
			for(int ch = 1; ch < length; ++ch){
				index.ch = ch;
				index.par = par[ch];
				index.type = types[ch];
				double change0 = 0.0 - manager.getScore(index);
				
				for(int pa = 0; pa < length; ++pa){
					if(ch ==pa || par[ch] == pa || isChild[ch][pa]){
						continue;
					}
					
					index.par = pa;
					manager.getType(inst, index, model);
					double change1 = manager.getScore(index);
					if(max < change0 + change1){
						max = change0 + change1;
						wh = ch;
						nPar = pa;
						nType = index.type;
					}
				}
			}
			if(max <= 0.0){
				break;
			}
			par[wh] = nPar;
			types[wh] = nType;
			isChild = calcChildren(par, length);
		}
	}
	
	@Override
	public double calcGradient(double[] gradient, Manager manager, ParserModel model, ObjectReader in1, ObjectReader in2) throws TrainingException{
		throw new TrainingException("SingleEdgeCLMDecoder does not support gradient calculation.");
	}
	
	@Override
	public Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException {
		throw new TrainingException("SingleEdgeCLMDecoder does not support marginals calculation.");
	}
}
