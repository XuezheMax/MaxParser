package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.ForestItem;
import maxparser.parser.decoder.forest.SiblingForest;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;
import maxparser.parser.indextuple.SiblingIndexTuple;
import maxparser.parser.manager.Manager;

public class SiblingProjDecoder extends SingleEdgeProjDecoder{
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		int length = inst.length();
		short zero = (short) 0;
		short one = (short) 1;
		short two = (short) 2;
		short minusOne = (short) -1;
		
		manager.getTypes(length, model);
		
		SiblingForest forest = new SiblingForest(length - 1, K);
		BasicForestIndexTuple forestIndex = new BasicForestIndexTuple();
		
		for(short s = 0; s < length; ++s){
			forestIndex.setIndex(s, s, zero, one);
			forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);
			
			forestIndex.setIndex(s, s, one, one);
			forest.addItem(forestIndex, minusOne, minusOne, 0.0, null, null);
		}
		
		SiblingIndexTuple index0 = new SiblingIndexTuple();
		SiblingIndexTuple index1 = new SiblingIndexTuple();
		
		for(short j = 1; j < length; ++j){
			for(short s = 0; s + j < length; ++s){
				short t = (short) (s + j);
				//positive index
				index0.par = s;
				index0.ch = t;
				
				//when r == s
				index0.ch1 = s;
				index0.type1 = -1;
				manager.getType(inst, index0, model);
				ForestItem[] b1 = forest.getItems(forestIndex.setIndex(s, s, zero, one));
				ForestItem[] c1 = forest.getItems(forestIndex.setIndex((short) (s + 1), t, one, one));
				int[][] pairs = forest.getKBestPairs(b1, c1);
				for(int k = 0; k < pairs.length; ++k){
					if(pairs[k][0] == -1 || pairs[k][1] == -1){
						break;
					}
					
					int comp1 = pairs[k][0];
					int comp2 = pairs[k][1];
					
					double score = b1[comp1].score + c1[comp2].score + manager.getScore(index0);
					boolean added = forest.addItem(forestIndex.setIndex(s, t, zero, zero), 
							s, (short) index0.type, score, b1[comp1], c1[comp2]);
					
					if(!added){
						break;
					}
				}
				
				//negative index
				index1.par = t;
				index1.ch = s;
				
				//when r == t
				index1.ch1 = t;
				index1.type1 = -1;
				manager.getType(inst, index1, model);
				b1 = forest.getItems(forestIndex.setIndex(s, (short) (t - 1), zero, one));
				c1 = forest.getItems(forestIndex.setIndex(t, t, one, one));
				pairs = forest.getKBestPairs(b1, c1);
				for(int k = 0; k < pairs.length; ++k){
					if(pairs[k][0] == -1 || pairs[k][1] == -1){
						break;
					}
					
					int comp1 = pairs[k][0];
					int comp2 = pairs[k][1];
					
					double score = b1[comp1].score + c1[comp2].score + manager.getScore(index1);
					boolean added = forest.addItem(forestIndex.setIndex(s, t, one, zero), 
							t, (short) index1.type, score, b1[comp1], c1[comp2]);
					
					if(!added){
						break;
					}
				}
				
				//create sibling
				for(short r = s; r < t; ++r){
					b1 = forest.getItems(forestIndex.setIndex(s, r, zero, one));
					c1 = forest.getItems(forestIndex.setIndex((short) (r + 1), t, one, one));
					pairs = forest.getKBestPairs(b1, c1);
					for(int k = 0; k < pairs.length; ++k){
						if(pairs[k][0] == -1 || pairs[k][1] == -1){
							break;
						}
						
						int comp1 = pairs[k][0];
						int comp2 = pairs[k][1];
						
						double score = b1[comp1].score + c1[comp2].score;
						
						boolean added1 = forest.addItem(forestIndex.setIndex(s, t, zero, two), 
								r, minusOne, score, b1[comp1], c1[comp2]);
						boolean added2 = forest.addItem(forestIndex.setIndex(s, t, one, two), 
								r, minusOne, score, b1[comp1], c1[comp2]);
						if(!added1 && !added2){
							break;
						}
					}
				}
				
				for(short r = (short) (s + 1); r < t; ++r){
					// s -> (r,t)
					b1 = forest.getItems(forestIndex.setIndex(s, r, zero, zero));
					c1 = forest.getItems(forestIndex.setIndex(r, t, zero, two));
					pairs = forest.getKBestPairs(b1, c1);
					for(int k = 0; k < pairs.length; ++k){
						if(pairs[k][0] == -1 || pairs[k][1] == -1){
							break;
						}
						
						int comp1 = pairs[k][0];
						int comp2 = pairs[k][1];
						
						index0.ch1 = r;
						index0.type1 = b1[comp1].type;
						manager.getType(inst, index0, model);
						double score = b1[comp1].score + c1[comp2].score + manager.getScore(index0);
						boolean added = forest.addItem(forestIndex.setIndex(s, t, zero, zero), 
								r, (short) index0.type, score, b1[comp1], c1[comp2]);
						if(!added){
							break;
						}
					}
					
					// t -> (r,s)
					b1 = forest.getItems(forestIndex.setIndex(s, r, one, two));
					c1 = forest.getItems(forestIndex.setIndex(r, t, one, zero));
					pairs = forest.getKBestPairs(b1, c1);
					for(int k = 0; k < pairs.length; ++k){
						if(pairs[k][0] == -1 || pairs[k][1] == -1){
							break;
						}
						
						int comp1 = pairs[k][0];
						int comp2 = pairs[k][1];
						
						index1.ch1 = r;
						index1.type1 = c1[comp2].type;
						manager.getType(inst, index1, model);
						double score = b1[comp1].score + c1[comp2].score + manager.getScore(index1);
						boolean added = forest.addItem(forestIndex.setIndex(s, t, one, zero), 
								r, (short) index1.type, score, b1[comp1], c1[comp2]);
						if(!added){
							break;
						}
					}
				}
				
				//incom + comp -> comp
				for(short r = s; r <= t; ++r){
					if(r != s){
						b1 = forest.getItems(forestIndex.setIndex(s, r, zero, zero));
						c1 = forest.getItems(forestIndex.setIndex(r, t, zero, one));
						pairs = forest.getKBestPairs(b1, c1);
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
						b1 = forest.getItems(forestIndex.setIndex(s, r, one, one));
						c1 = forest.getItems(forestIndex.setIndex(r, t, one, zero));
						pairs = forest.getKBestPairs(b1, c1);
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
