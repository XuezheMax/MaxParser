package maxparser.parser.manager;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SiblingIndexTuple;
import maxparser.parser.manager.featgen.SiblingFeatureGenerator;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public class SiblingManager extends SingleEdgeManager{
	
	public SiblingManager(){
		featGen = new SiblingFeatureGenerator();
	}
	
	protected double[][][] probs_sibs = null;
	protected double[][][] probs_trips = null;
	
	@Override
	public void init(int size) {
		super.init(size);
		probs_sibs = new double[size][size][2];
		probs_trips = new double[size][size][];
		for(int i = 0; i < size; ++i){
			for(int j = 0; j < size; ++j){
				int n = j < i ? j : size;
				probs_trips[i][j] = new double[n];
			}
		}
	}
	
	@Override
	protected void writeUnlabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException{
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
	public DependencyInstance readUnlabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException{
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
	
	@Override
	public Manager clone(int size) {
		SiblingManager manager = new SiblingManager();
		manager.init(size);
		return manager;
	}
}
