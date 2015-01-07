package maxparser.parser.typelabler;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;

public class DefaultTypeLabeler extends TypeLabeler{
	
	public DefaultTypeLabeler(){}
	
	@Override
	public void init(int maxLength){}
	
	@Override
	public void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv){}

	@Override
	public void writeLabeledInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model) {}

	@Override
	public void readLabeledInstance(ObjectInputStream in, ParserModel model) {}

	@Override
	public void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model) {}

	@Override
	public double getLabeledScore(IndexTuple itemId) {
		return 0;
	}

	@Override
	public int getType(IndexTuple itemId) {
		return 0;
	}

	@Override
	public void getTypes(int length) {}
	
	
}
