package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.SingleEdgeForest;
import maxparser.parser.decoder.forest.ForestItem;
import maxparser.parser.manager.Manager;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;

public class SingleEdgeProjDecoder extends Decoder{

	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		int length = inst.length();
		short zero = (short) 0;
		short one = (short) 1;
		short minusOne = (short) -1;
		
		manager.getTypes(length, model);
		
		SingleEdgeForest forest = new SingleEdgeForest(length - 1, K);
		BasicForestIndexTuple forestIndex = new BasicForestIndexTuple();
		
		for(short s = 0; s < length; ++s){
			forestIndex.setIndex(s, s, zero, one);
			forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);
			
			forestIndex.setIndex(s, s, one, one);
			forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);
		}
		
		SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
		SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();
		
		for(short j = 1; j < length; ++j){
			for(short s = 0; s + j < length; ++s){
				short t = (short) (s + j);
				//positive index
				index0.par = s;
				index0.ch = t;
				manager.getType(inst, index0, model);
				//negative index
				index1.par = t;
				index1.ch = s;
				manager.getType(inst, index1, model);
				
				for(short r = s; r < t; ++r){
					ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, zero, one));
					ForestItem[] c1 = forest.getItems(forestIndex.setIndex((short)(r + 1), t, one, one));
					int[][] pairs = forest.getKBestPairs(b1, c1);
					for(int k = 0; k < pairs.length; ++k){
						if(pairs[k][0] == -1 || pairs[k][1] == -1){
							break;
						}
						
						int comp1 = pairs[k][0];
						int comp2 = pairs[k][1];
						
						double score = b1[comp1].score + c1[comp2].score + manager.getScore(index0);
						boolean added1 = forest.addItem(forestIndex.setIndex(s, t, zero, zero), 
								r, (short) index0.type, score, b1[comp1], c1[comp2]);
						
						score = b1[comp1].score + c1[comp2].score + manager.getScore(index1);
						boolean added2 = forest.addItem(forestIndex.setIndex(s, t, one, zero), 
								r, (short) index1.type, score, b1[comp1], c1[comp2]);
						
						if(!added1 && !added2){
							break;
						}
					}
				}
				for(short r = s; r <= t; ++r){
					if(r != s){
						ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, zero, zero));
						ForestItem[] c1 = forest.getItems(forestIndex.setIndex(r, t, zero, one));
						int[][] pairs = forest.getKBestPairs(b1, c1);
						for(int k = 0; k < pairs.length; ++k){
							if(pairs[k][0] == -1 || pairs[k][1] == -1){
								break;
							}
							
							int comp1 = pairs[k][0];
							int comp2 = pairs[k][1];
							
							double score = b1[comp1].score + c1[comp2].score;
							if(!forest.addItem(forestIndex.setIndex(s, t, zero, one), r, minusOne, score, b1[comp1], c1[comp2])){
								break;
							}
						}
					}
					if(r != t){
						ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, r, one, one));
						ForestItem[] c1 = forest.getItems(forestIndex.setIndex(r, t, one, zero));
						int[][] pairs = forest.getKBestPairs(b1, c1);
						for(int k = 0; k < pairs.length; ++k){
							if(pairs[k][0] == -1 || pairs[k][1] == -1){
								break;
							}
							
							int comp1 = pairs[k][0];
							int comp2 = pairs[k][1];
							
							double score = b1[comp1].score + c1[comp2].score;
							if(!forest.addItem(forestIndex.setIndex(s, t, one, one), r, minusOne, score, b1[comp1], c1[comp2])){
								break;
							}
						}
					}
				}
			}
		}
		return forest.getBestParses(inst, manager, model);
	}
}
