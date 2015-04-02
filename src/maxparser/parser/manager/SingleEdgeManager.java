package maxparser.parser.manager;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.manager.featgen.SingleEdgeFeatureGenerator;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public class SingleEdgeManager extends Manager{
	
	public SingleEdgeManager(){
		featGen = new SingleEdgeFeatureGenerator();
	}
	
	protected double[][] probs = null;

	@Override
	protected void writeUnlabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException{
		//write feature vector of current instance
		out.writeObject(inst.getFeatureVector().keys());
		out.writeInt(-4);
		
		//write current instance itself
		out.writeObject(inst);
		out.writeInt(-1);
		out.reset();
		
		//write feature vectors of all possible edges
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				FeatureVector fv = new FeatureVector();
				((SingleEdgeFeatureGenerator) featGen).addSingleEdgeFeatures(inst, par, ch, model, fv);
				out.writeObject(fv.keys());
			}
		}
		out.writeInt(-3);
	}

	@Override
	public DependencyInstance readUnlabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException{
		//read feature vector of current instance
		FeatureVector nfv = new FeatureVector((int[]) in.readObject());
		int last = in.readInt();
		if(last != -4){
			throw new IOException("last number is not equal to -4");
		}
		
		//read current instance itself
		DependencyInstance inst = (DependencyInstance) in.readObject();
		last = in.readInt();
		if(last != -1){
			throw new IOException("last number is not equal to -1");
		}
		inst.setFeatureVector(nfv);
		
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				FeatureVector fv = new FeatureVector((int[]) in.readObject());
				probs[par][ch] = model.getScore(fv);
			}
		}
		last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		return inst;
	}

	@Override
	public void init(int size) {
		probs = new double[size][size];
	}

	@Override
	protected void fillUnlabeledFeatureVector(DependencyInstance inst, ParserModel model) {
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				FeatureVector fv = new FeatureVector();
				((SingleEdgeFeatureGenerator) featGen).addSingleEdgeFeatures(inst, par, ch, model, fv);
				probs[par][ch] = model.getScore(fv);
			}
		}
	}

	@Override
	public double getUnlabeledScore(IndexTuple itemId) {
		SingleEdgeIndexTuple id = (SingleEdgeIndexTuple) itemId;
		return probs[id.par][id.ch];
	}

	@Override
	public void adjustEdgeLoss(DependencyInstance inst, ParserModel model) {
		boolean nopunc = model.nopunc();
		int length = inst.length();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(par == ch){
					continue;
				}
				if(!nopunc || !model.isPunct(inst.postags[ch])){
					probs[par][ch] += 1.0;
				}
			}
		}
		
		for(int i = 1; i < length; ++i){
			if(!nopunc || !model.isPunct(inst.postags[i])){
				probs[inst.heads[i]][i] -= 1.0;
			}
		}
	}

	@Override
	public int size() {
		return probs.length;
	}

	@Override
	public Manager clone(int size, int type_size) {
		SingleEdgeManager manager = new SingleEdgeManager();
		manager.init(size);
		manager.setTypeLabeler(typelabeler.clone(size, type_size));
		return manager;
	}
}
