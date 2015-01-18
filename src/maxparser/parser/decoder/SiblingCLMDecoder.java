package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.SiblingIndexTuple;
import maxparser.parser.manager.Manager;

public class SiblingCLMDecoder extends SingleEdgeCLMDecoder{
	
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		Pair<FeatureVector, String>[] d = new SiblingProjDecoder().decode(manager, inst, K, model);
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
	
	@Override
	protected void rearrange(DependencyInstance inst, Manager manager, int[] par, int[] types, int length, ParserModel model){
		boolean[][] isChild = calcChildren(par, length);
		SiblingIndexTuple index = new SiblingIndexTuple();
		while(true){
			int wh = -1, nPar = -1, nType = -1;
			double max = Double.NEGATIVE_INFINITY;
			for(int ch = 1; ch < length; ++ch){
				int aSib = getASib(ch, par, length);
				int bSib = getBSib(ch, par, length);
				//(par, aSib, ch, type[aSib], type[ch])
				index.par = par[ch];
				index.ch1 = aSib;
				index.ch = ch;
				index.type1 = types[aSib];
				index.type = types[ch];
				double change0 = 0.0 - manager.getScore(index);
				
				int nbType = types[bSib];
				if(bSib != ch){
					//(par, ch, bSib, type[ch], type[bSib])
					index.ch1 = ch;
					index.ch = bSib;
					index.type1 = types[ch];
					index.type = types[bSib];
					change0 -= manager.getScore(index);
					//(par, aSib, bSib, type[aSib], type[bSib])
					index.ch1 = aSib;
					index.ch = bSib;
					index.type1 = types[aSib];
					manager.getType(inst, index);
					change0 += manager.getScore(index);
					nbType = index.type;
					
					//get sibling changes
					change0 += getSibChange(inst, manager, bSib, nbType, par, types, length, model);
				}
				
				int oP = par[ch];
				for(int pa = 0; pa < length; ++pa){
					if(ch ==pa || par[ch] == pa || isChild[ch][pa]){
						continue;
					}
					par[ch] = pa;
					aSib = getASib(ch, par, length);
					bSib = getBSib(ch, par, length);
					
					//(pa, aSib, ch, type[aSib], typeCh)
					index.par = pa;
					index.ch1 = aSib;
					index.ch = ch;
					index.type1 = types[aSib];
					manager.getType(inst, index);
					int typeCh = index.type;
					double change1 = manager.getScore(index);
					nbType = types[bSib];
					if(ch != bSib){
						//(pa, ch, bSib, typeCh, nbType)
						index.ch1 = ch;
						index.ch = bSib;
						index.type1 = typeCh;
						manager.getType(inst, index);
						change1 += manager.getScore(index);
						nbType = index.type;
						
						//(pa, aSib, bSib, type[aSib], type[bSib])
						index.ch1 = aSib;
						index.type1 = types[aSib];
						index.type = types[bSib];
						change1 -= manager.getScore(index);
						
						change1 += getSibChange(inst, manager, bSib, nbType, par, types, length, model);
					}
					if(max < change0 + change1){
						max = change0 + change1;
						wh = ch;
						nPar = pa;
						nType = typeCh;
					}
					par[ch] = oP;
				}
			}
			if(max <= 0.0){
				break;
			}
			updateTree(inst, manager, wh, nPar, nType, par, types, length, model);
			isChild = calcChildren(par, length);
		}
	}
	
	protected double getSibChange(DependencyInstance inst, Manager manager, int bSib, int nbType, int par[], int types[], int length, ParserModel model){
		if(!model.labeled() || nbType == types[bSib]){
			return 0.0;
		}
		else{
			double change = 0.0;
			int pa = par[bSib];
			int ch = bSib;
			bSib = getBSib(ch, par, length);
			while(bSib != ch){
				SiblingIndexTuple old_index = new SiblingIndexTuple(pa, ch, bSib, types[ch], types[bSib]);
				change -= manager.getScore(old_index);
				SiblingIndexTuple new_index = new SiblingIndexTuple(pa, ch, bSib, nbType, -1);
				manager.getType(inst, new_index);
				change += manager.getScore(new_index);
				nbType = new_index.type;
				if(nbType == types[bSib]){
					break;
				}
				ch = bSib;
				bSib = getBSib(ch, par, length);
			}
			return change;
		}
	}
	
	protected void updateTree(DependencyInstance inst, Manager manager, int ch, int nPar, int nType, int par[], int types[], int length, ParserModel model){
		if(!model.labeled()){
			par[ch] = nPar;
			types[ch] = nType;
			return;
		}
		
		//remove edge
		int pa = par[ch];
		int aSib = getASib(ch, par, length);
		int bSib = getBSib(ch, par, length);
		int nbType = types[bSib];
		SiblingIndexTuple index = new SiblingIndexTuple(pa, aSib, bSib, types[aSib], -1);
		if(bSib != ch){
			manager.getType(inst, index);
			nbType = index.type;
		}
		while(nbType != types[bSib]){
			types[bSib] = nbType;
			index.ch1 = bSib;
			bSib = getBSib(bSib, par, length);
			index.ch = bSib;
			index.type1 = types[index.ch1];
			nbType = types[bSib];
			if(bSib != index.ch1){
				manager.getType(inst, index);
				nbType = index.type;
			}
		}
		
		//add new edge
		par[ch] = nPar;
		types[ch] = nType;
		pa = par[ch];
		bSib = getBSib(ch, par, length);
		nbType = types[bSib];
		index = new SiblingIndexTuple(pa, ch, bSib, types[ch], -1);
		if(bSib != ch){
			manager.getType(inst, index);
			nbType = index.type;
		}
		while(nbType != types[bSib]){
			types[bSib] = nbType;
			index.ch1 = bSib;
			bSib = getBSib(bSib, par, length);
			index.ch = bSib;
			nbType = types[bSib];
			if(bSib != index.ch1){
				manager.getType(inst, index);
				nbType = index.type;
			}
		}
	}
	
	protected int getASib(int ch, int[] par, int length){
		int aSib = par[ch];
		if(par[ch] > ch){
			for(int i = ch + 1; i < par[ch]; ++i){
				if(par[i] == par[ch]){
					aSib = i;
					break;
				}
			}
		}
		else{
			for(int i = ch - 1; i > par[ch]; --i){
				if(par[i] == par[ch]){
					aSib = i;
					break;
				}
			}
		}
		return aSib;
	}
	
	protected int getBSib(int ch, int[] par, int length){
		int bSib = ch;
		if(par[ch] > ch){
			for(int i = ch - 1; i >= 0; --i){
				if(par[i] == par[ch]){
					bSib = i;
					break;
				}
			}
		}
		else{
			for(int i = ch + 1; i < length; ++i){
				if(par[i] == par[ch]){
					bSib = i;
					break;
				}
			}
		}
		return bSib;
	}
}
