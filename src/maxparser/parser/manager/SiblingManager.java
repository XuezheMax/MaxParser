package maxparser.parser.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.featgen.SiblingFeatureGenerator;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SiblingIndexTuple;

public class SiblingManager extends SingleEdgeManager{
	
	public SiblingManager(){
		featGen = new SiblingFeatureGenerator();
	}
	
	protected double[][][] probs_sibs = null;
	protected double[][][] probs_trips = null;
	
	@Override
	public void init(int maxLength) {
		super.init(maxLength);
		probs_sibs = new double[maxLength][maxLength][2];
		probs_trips = new double[maxLength][maxLength][];
		for(int i = 0; i < maxLength; ++i){
			for(int j = 0; j < maxLength; ++j){
				int n = j < i ? j : maxLength;
				probs_trips[i][j] = new double[n];
			}
		}
	}
	
	@Override
	protected void writeUnlabeledInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model) throws IOException{
		super.writeUnlabeledInstance(inst, out, model);
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch1 = par; ch1 < length; ++ch1){
				for(int ch2 = ch1 + 1; ch2 < length; ++ch2){
					FeatureVector fv = new FeatureVector();
					((SiblingFeatureGenerator) featGen).addTripFeatures(inst, par, ch1, ch2, model, fv);
					out.writeObject(fv.keys());
				}
			}
			for(int ch1 = par; ch1 >= 0; --ch1){
				for(int ch2 = ch1 - 1; ch2 >= 0; --ch2){
					FeatureVector fv = new FeatureVector();
					((SiblingFeatureGenerator) featGen).addTripFeatures(inst, par, ch1, ch2, model, fv);
					out.writeObject(fv.keys());
				}
			}
		}
		out.writeInt(-3);
		
		for(int ch1 = 0; ch1 < length; ++ch1){
			for(int ch2 = 0; ch2 < length; ++ch2){
				for(int wh = 0; wh < 2; ++wh){
					if(ch1 != ch2){
						FeatureVector fv = new FeatureVector();
						((SiblingFeatureGenerator) featGen).addSiblingFeatures(inst, ch1, ch2, wh == 0, model, fv);
						out.writeObject(fv.keys());
					}
				}
			}
		}
		out.writeInt(-3);
	}
	
	@Override
	public DependencyInstance readUnlabeledInstance(ObjectInputStream in, ParserModel model) throws IOException, ClassNotFoundException{
		DependencyInstance inst = super.readUnlabeledInstance(in, model);
		
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch1 = par; ch1 < length; ++ch1){
				for(int ch2 = ch1 + 1; ch2 < length; ++ch2){
					FeatureVector fv = new FeatureVector((int[]) in.readObject());
					probs_trips[par][ch1][ch2] = model.getScore(fv);
				}
			}
			for(int ch1 = par; ch1 >= 0; --ch1){
				for(int ch2 = ch1 - 1; ch2 >= 0; --ch2){
					FeatureVector fv = new FeatureVector((int[]) in.readObject());
					probs_trips[par][ch1][ch2] = model.getScore(fv);
				}
			}
		}
		int last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		
		for(int ch1 = 0; ch1 < length; ++ch1){
			for(int ch2 = 0; ch2 < length; ++ch2){
				for(int wh = 0; wh < 2; ++wh){
					if(ch1 != ch2){
						FeatureVector fv = new FeatureVector((int[]) in.readObject());
						probs_sibs[ch1][ch2][wh] = model.getScore(fv);
					}
				}
			}
		}
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		return inst;
	}
	
	@Override
	protected void fillUnlabeledFeatureVector(DependencyInstance inst, ParserModel model) {
		super.fillFeatureVector(inst, model);
		
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch1 = par; ch1 < length; ++ch1){
				for(int ch2 = ch1 + 1; ch2 < length; ++ch2){
					FeatureVector fv = new FeatureVector();
					((SiblingFeatureGenerator) featGen).addTripFeatures(inst, par, ch1, ch2, model, fv);
					probs_trips[par][ch1][ch2] = model.getScore(fv);
				}
			}
			for(int ch1 = par; ch1 >= 0; --ch1){
				for(int ch2 = ch1 - 1; ch2 >= 0; --ch2){
					FeatureVector fv = new FeatureVector();
					((SiblingFeatureGenerator) featGen).addTripFeatures(inst, par, ch1, ch2, model, fv);
					probs_trips[par][ch1][ch2] = model.getScore(fv);
				}
			}
		}
		
		for(int ch1 = 0; ch1 < length; ++ch1){
			for(int ch2 = 0; ch2 < length; ++ch2){
				for(int wh = 0; wh < 2; ++wh){
					if(ch1 != ch2){
						FeatureVector fv = new FeatureVector();
						((SiblingFeatureGenerator) featGen).addSiblingFeatures(inst, ch1, ch2, wh == 0, model, fv);
						probs_sibs[ch1][ch2][wh] = model.getScore(fv);
					}
				}
			}
		}
	}
	
	@Override
	public double getUnlabeledScore(IndexTuple itemId) {
		double score = super.getUnlabeledScore(itemId);
		SiblingIndexTuple id = (SiblingIndexTuple) itemId;
		return score + probs_trips[id.par][id.ch1][id.ch] + probs_sibs[id.ch1][id.ch][id.par == id.ch1 ? 0 : 1] ;
	}
}
