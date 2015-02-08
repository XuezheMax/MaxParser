package maxparser.parser.decoder;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.Util;
import maxparser.exception.TrainingException;
import maxparser.io.ObjectReader;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.SingleEdgeForest;
import maxparser.parser.decoder.forest.ForestItem;
import maxparser.parser.manager.Manager;
import maxparser.parser.marginal.Marginal;
import maxparser.parser.decoder.forest.indextuple.BasicForestIndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.marginal.ioforest.InOutForest;
import maxparser.parser.marginal.ioforest.SingleEdgeInOutForest;

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

	@Override
	public double calcGradient(double[] gradient, Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException{
		long offset = in.tell();
		DependencyInstance inst = manager.readInstance(in, model);
		in.seek(offset);
		
		double obj = 0.0;
		SingleEdgeInOutForest ioForest = new SingleEdgeInOutForest(inst.length());
		
		double z = inside(inst.length(), ioForest, manager);
		
		outside(inst.length(), ioForest, manager);
		
		obj = z - model.getScore(inst.getFeatureVector());
		
		//calc gradient
		getGradient(gradient, ioForest, z, inst.length(), manager, model, in);
		
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
		
		BasicForestIndexTuple index = new BasicForestIndexTuple();
		for(short par = 0; par < length; ++par){
			for(short ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
						
				keys = (int[]) in.readObject();
				index.setIndex(par, ch, (short) (par < ch ? 0 : 1), (short) 0);
				double m = Math.exp(ioForest.getBeta(index) + ioForest.getAlpha(index) - z);
				updateGradient(gradient, keys, m);
			}
		}
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
	}

	protected double inside(int length, InOutForest ioForest, Manager manager) {
		short zero = (short) 0;
		short one = (short) 1;
		BasicForestIndexTuple fid00 = new BasicForestIndexTuple();
		BasicForestIndexTuple fid01 = new BasicForestIndexTuple();
		BasicForestIndexTuple fid10 = new BasicForestIndexTuple();
		BasicForestIndexTuple fid11 = new BasicForestIndexTuple();
		
		BasicForestIndexTuple fid = new BasicForestIndexTuple();
		
//		for(short s = 0; s < length; ++s){
//			index.setIndex(s, s, zero, one);
//			ioForest.addBeta(index, 0.0);
//			index.setIndex(s, s, one, one);
//			ioForest.addBeta(index, 0.0);
//		}
		
		SingleEdgeIndexTuple index0 = new SingleEdgeIndexTuple();
		SingleEdgeIndexTuple index1 = new SingleEdgeIndexTuple();
		
		for(short j = 1; j < length; ++j){
			for(short s = 0; s + j < length; ++s){
				short t = (short) (s + j);
				index0.par = s;
				index0.ch = t;
				
				//negative index
				index1.par = t;
				index1.ch = s;
				
				//init beta
				//incomplete spans
				ioForest.addBeta(fid00.setIndex(s, t, zero, zero), Double.NEGATIVE_INFINITY);
				ioForest.addBeta(fid10.setIndex(s, t, one, zero), Double.NEGATIVE_INFINITY);
				
				//complete spans
				ioForest.addBeta(fid01.setIndex(s, t, zero, one), Double.NEGATIVE_INFINITY);
				ioForest.addBeta(fid11.setIndex(s, t, one, one), Double.NEGATIVE_INFINITY);
				
				for(short r = s; r < t; ++r){
					double val = Util.logsumexp(ioForest.getBeta(fid00), 
							ioForest.getBeta(fid.setIndex(s, r, zero, one)) 
							+ ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one))
							+ manager.getScore(index0));
					ioForest.addBeta(fid00, val);
					
					val = Util.logsumexp(ioForest.getBeta(fid10), 
							ioForest.getBeta(fid.setIndex(s, r, zero, one)) 
							+ ioForest.getBeta(fid.setIndex((short) (r + 1), t, one, one))
							+ manager.getScore(index1));
					ioForest.addBeta(fid10, val);
				}
				
				for(short r = s; r <= t; ++r){
					if(r != s){
						double val = Util.logsumexp(ioForest.getBeta(fid01), 
								ioForest.getBeta(fid.setIndex(s, r, zero, zero)) 
								+ ioForest.getBeta(fid.setIndex(r, t, zero, one)));
						ioForest.addBeta(fid01, val);
					}
					
					if(r != t){
						double val = Util.logsumexp(ioForest.getBeta(fid11), 
								ioForest.getBeta(fid.setIndex(s, r, one, one)) 
								+ ioForest.getBeta(fid.setIndex(r, t, one, zero)));
						ioForest.addBeta(fid11, val);
					}
				}
			}
		}
		return Util.logsumexp(ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), zero, one)), 
				ioForest.getBeta(fid.setIndex(zero, (short) (length - 1), one, one)));
	}

	protected void outside(int length, InOutForest ioForest, Manager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Marginal calcMarginals(Manager manager, ParserModel model, ObjectReader in) throws TrainingException, IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
