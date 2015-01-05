package maxparser.parser.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.featgen.FirstOrderFeatureGenerator;
import maxparser.parser.indextuple.FirstOrderIndexTuple;
import maxparser.parser.indextuple.IndexTuple;

public class FirstOrderManager extends Manager{
	
	protected double[][] probs = null;

	@Override
	protected void writeUnlabeledInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model) throws IOException{
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
				((FirstOrderFeatureGenerator) featGen).addSingleEdgeFeatures(inst, par, ch, model, fv);
				out.writeObject(fv.keys());
			}
		}
		out.writeInt(-3);
	}

	@Override
	public DependencyInstance readUnlabeledInstance(ObjectInputStream in, ParserModel model) throws IOException, ClassNotFoundException{
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
		return inst;
	}

	@Override
	public void init(int maxLength) {
		probs = new double[maxLength][maxLength];
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
				((FirstOrderFeatureGenerator) featGen).addSingleEdgeFeatures(inst, par, ch, model, fv);
				probs[par][ch] = model.getScore(fv);
			}
		}
	}

	@Override
	public double getScore(IndexTuple itemId) {
		FirstOrderIndexTuple id = (FirstOrderIndexTuple) itemId;
		return probs[id.par][id.ch];
	}
}
