package maxparser.parser.typelabler;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.typelabler.featgen.LabeledFeatureGenerator;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public abstract class TypeLabeler {
	
	protected LabeledFeatureGenerator featGen = null;
	
	public abstract void init(int maxLength, int type_size);
	
	public abstract void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv);
	
	public abstract void writeLabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException;
	
	public abstract int readLabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException;
	
	public abstract void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model);
	
	public abstract double getLabeledScore(IndexTuple itemId);
	
	public abstract void getType(DependencyInstance inst, IndexTuple itemId, ParserModel model);
	
	public abstract void getTypes(int length, ParserModel model);
}
