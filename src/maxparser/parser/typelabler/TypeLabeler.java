package maxparser.parser.typelabler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;

public abstract class TypeLabeler {
	
	public abstract void init(int maxLength);
	
	public abstract void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv);
	
	public abstract void writeLabeledInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model) throws IOException;
	
	public abstract void readLabeledInstance(ObjectInputStream in, ParserModel model) throws IOException;
	
	public abstract void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model);
	
	public abstract double getLabeledScore(IndexTuple itemId);
	
	public abstract void getType(DependencyInstance inst, IndexTuple itemId);
	
	public abstract void getTypes(int length);
}
