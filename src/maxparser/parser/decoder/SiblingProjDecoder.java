package maxparser.parser.decoder;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.ForestItem;
import maxparser.parser.decoder.forest.SiblingForest;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;
import maxparser.parser.indextuple.SiblingIndexTuple;
import maxparser.parser.manager.Manager;
import maxparser.parser.marginal.Marginal;
import maxparser.parser.marginal.ioforest.InOutForest;
import maxparser.parser.marginal.ioforest.SiblingInOutForest;

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
	
	@Override
	public double calcGradient(double[] gradient, Manager manager, ParserModel model, ObjectReader in1, ObjectReader in2) throws TrainingException, IOException, ClassNotFoundException{
		DependencyInstance inst = manager.readInstance(in1, model);
		
		double obj = 0.0;
		SiblingInOutForest ioForest = new SiblingInOutForest(inst.length());
		
		double z = inside(inst.length(), ioForest, manager);
		
		outside(inst.length(), ioForest, manager);
		
		obj = z - model.getScore(inst.getFeatureVector());
		
		//calc gradient
		getGradient(gradient, ioForest, z, inst.length(), manager, model, in2);
		
		return obj;
	}
	
	protected void getGradient(double[] gradient, InOutForest ioForest, double z, int length, Manager manager, ParserModel model, ObjectReader in) throws ClassNotFoundException, IOException{
		//read feature vector of current instance
		int[] keys = (int[]) in.readObject();
		int last = in.readInt();
		if(last != -4){
			throw new IOException("last number is not equal to -4");
		}
		updateGradient(gradient, keys, -1.0);
		//read current instance itself
		in.readObject();
		last = in.readInt();
		if(last != -1){
			throw new IOException("last number is not equal to -1");
		}
		
		BasicForestIndexTuple fidA = new BasicForestIndexTuple();
		BasicForestIndexTuple fidB1 = new BasicForestIndexTuple();
		BasicForestIndexTuple fidB2 = new BasicForestIndexTuple();
		
		SiblingIndexTuple index = new SiblingIndexTuple();
		
		//probs
		for(short par = 0; par < length; ++par){
			for(short ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				
				index.par = par;
				index.ch1 = par;
				index.ch = ch;
				
				keys = (int[]) in.readObject();
				short min = par < ch ? par : ch;
				short max = par < ch ? ch : par;
				short ph = (short) (par < ch ? 0 : 1);
				
				double m = 0.0;
				fidA.setIndex(min, max, ph, (short) 0);
				if(ph == 0){
					fidB1.setIndex((short) (min + 1), max, (short) 1, (short) 1);
				}
				else{
					fidB1.setIndex(min, (short) (max - 1), (short) 0, (short) 1);
				}
				m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
				for(short r = (short) (min + 1); r < max; ++r){
					index.ch1 = r;
					if(ph == 0){
						fidB1.setIndex(min, r, ph, (short) 0);
						fidB2.setIndex(r, max, (short) 0, (short) 2);
					}
					else{
						fidB1.setIndex(min, r, (short) 0, (short) 2);
						fidB2.setIndex(r, max, ph, (short) 0);
					}
					m += Math.exp(ioForest.getBeta(fidB1) + ioForest.getBeta(fidB2) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
				}
				updateGradient(gradient, keys, m);
			}
		}
		
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		
		//trips
		for(short par = 0; par < length; ++par){
			for(short ch1 = par; ch1 < length; ++ch1){
				for(short ch2 = (short) (ch1 + 1); ch2 < length; ++ch2){
					index.par = par;
					index.ch1 = ch1;
					index.ch = ch2;
					
					keys = (int[]) in.readObject();
					double m = 0.0;
					fidA.setIndex(par, ch2, (short) 0, (short) 0);
					if(par == ch1){
						fidB1.setIndex((short) (par + 1), ch2, (short) 1, (short) 1);
						m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
					}
					else{
						fidB1.setIndex(par, ch1, (short) 0, (short) 0);
						fidB2.setIndex(ch1, ch2, (short) 0, (short) 2);
						m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getBeta(fidB2) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
					}
					updateGradient(gradient, keys, m);
				}
			}
			
			for(short ch1 = par; ch1 >= 0; --ch1){
				for(short ch2 = (short) (ch1 - 1); ch2 >= 0; --ch2){
					keys = (int[]) in.readObject();
					double m = 0.0;
					fidA.setIndex(ch2, par, (short) 1, (short) 0);
					if(par == ch1){
						fidB1.setIndex(ch2, (short) (par - 1), (short) 0, (short) 1);
						m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
					}
					else{
						fidB1.setIndex(ch2, ch1, (short) 0, (short) 2);
						fidB2.setIndex(ch1, par, (short) 1, (short) 0);
						m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getBeta(fidB2) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
					}
					updateGradient(gradient, keys, m);
				}
			}
		}
		
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		
		//sibs
		for(short ch1 = 0; ch1 < length; ++ch1){
			for(short ch2 = 0; ch2 < length; ++ch2){
				for(short wh = 0; wh < 2; ++wh){
					if(ch1 != ch2){
						keys = (int[]) in.readObject();
						
						short min = ch1 < ch2 ? ch1 : ch2;
						short max = ch1 < ch2 ? ch2 : ch1;
						short ph = (short) (ch1 < ch2 ? 0 : 1);
						double m = 0.0;
						if(wh == 0){
							index.par = ch1;
							index.ch1 = ch1;
							index.ch = ch2;
							
							fidA.setIndex(min, max, ph, (short) 0);
							if(ph == 0){
								fidB1.setIndex((short) (min + 1), max, (short) 1, (short) 1);
							}
							else{
								fidB1.setIndex(min, (short) (max - 1), (short) 0, (short) 1);
							}
							m = Math.exp(ioForest.getBeta(fidB1) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
						}
						else{
							index.ch1 = ch1;
							index.ch = ch2;
							
							if(ph == 0){
								for(short par = 0; par < ch1; ++par){
									index.par = par;
									fidA.setIndex(par, ch2, ph, (short) 0);
									fidB1.setIndex(par, ch1, ph, (short) 0);
									fidB2.setIndex(min, max, (short) 0, (short) 2);
									m += Math.exp(ioForest.getBeta(fidB1) + ioForest.getBeta(fidB2) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
								}
							}
							else{
								for(short par = (short) (ch1 + 1); par < length; ++par){
									index.par = par;
									fidA.setIndex(ch2, par, ph, (short) 0);
									fidB1.setIndex(ch1, par, ph, (short) 0);
									fidB2.setIndex(min, max, (short) 0, (short) 2);
									m += Math.exp(ioForest.getBeta(fidB1) + ioForest.getBeta(fidB2) + ioForest.getAlpha(fidA) + manager.getScore(index) - z);
								}
							}
						}
						updateGradient(gradient, keys, m);
					}
				}
			}
		}
		
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
	}

	@Override
	protected double inside(int length, InOutForest ioForest, Manager manager) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void outside(int length, InOutForest ioForest, Manager manager) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
