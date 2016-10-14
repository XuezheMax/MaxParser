package maxparser.parser.typelabeler;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.indextuple.SingleEdgeIndexTuple;
import maxparser.parser.typelabeler.featgen.SingleEdgeLabeledFeatureGenerator;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public class SingleEdgeTypeLabeler extends SimpleTypeLabeler{

	protected double[][][] lab_probs = null;
	
	public SingleEdgeTypeLabeler(){
		featGen = new SingleEdgeLabeledFeatureGenerator();
	}
	
	@Override
	public void init(int maxLength, int type_size) {
		super.init(maxLength, type_size);
		lab_probs = new double[maxLength][maxLength][type_size];
	}

	@Override
	public void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv) {
		for(int i = 0; i < inst.length(); ++i){
			if(inst.heads[i] == -1){
				continue;
			}
			
			boolean attR = i < inst.heads[i] ? false : true;
			
			//single label features
			((SingleEdgeLabeledFeatureGenerator) featGen).addChildSingleLabelFeatures(inst, i, inst.deprelIds[i], attR, model, fv);
			((SingleEdgeLabeledFeatureGenerator) featGen).addParentSingleLabelFeatures(inst, inst.heads[i], inst.deprelIds[i], attR, model, fv);
			
			//linear labeled features
			((SingleEdgeLabeledFeatureGenerator) featGen).addSingleEdgeLabeledFeatures(inst, inst.heads[i], i, inst.deprelIds[i], model, fv);
		}
	}

	@Override
	public void writeLabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException {
		super.writeLabeledInstance(inst, out, model);
	    
	    int length = inst.length();
	    int type_size = model.typeSize();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				for(int t = 0; t < type_size; ++t){
					FeatureVector fv = new FeatureVector();
					((SingleEdgeLabeledFeatureGenerator) featGen).addSingleEdgeLabeledFeatures(inst, par, ch, t, model, fv);
					out.writeObject(fv.keys());
				}
			}
		}
		out.writeInt(-3);
	}

	@Override
	public int readLabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException {
		int length = super.readLabeledInstance(in, model);
		int type_size = model.typeSize();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				for(int t = 0; t < type_size; ++t){
					FeatureVector fv = new FeatureVector((int[]) in.readObject());
					lab_probs[par][ch][t] = model.getScore(fv);
				}
			}
		}
		int last = in.readInt();
		if(last != -3){
			throw new IOException("last number is not equal to -3");
		}
		return length;
	}

	@Override
	public void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model) {
	    super.fillLabeledFeatureVector(inst, model);
	    
		int length = inst.length();
		int type_size = model.typeSize();
		for(int par = 0; par < length; ++par){
			for(int ch = 0; ch < length; ++ch){
				if(ch == par){
					continue;
				}
				for(int t = 0; t < type_size; ++t){
					FeatureVector fv = new FeatureVector();
					((SingleEdgeLabeledFeatureGenerator) featGen).addSingleEdgeLabeledFeatures(inst, par, ch, t, model, fv);
					lab_probs[par][ch][t] = model.getScore(fv);
				}
			}
		}
	}

	@Override
	public double getLabeledScore(IndexTuple itemId) {
		SingleEdgeIndexTuple id = (SingleEdgeIndexTuple) itemId;
		int dir = id.par < id.ch ? 0 : 1;
		return nt_probs[id.par][id.type][dir][1] + nt_probs[id.ch][id.type][dir][0] + lab_probs[id.par][id.ch][id.type];
	}

	@Override
	public TypeLabeler clone(int size, int type_size) {
		SingleEdgeTypeLabeler typeLabeler = new SingleEdgeTypeLabeler();
		typeLabeler.init(size, type_size);
		return typeLabeler;
	}
}
