package maxparser.parser.typelabler;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;

public class DefaultTypeLabeler extends TypeLabeler{
	
	public DefaultTypeLabeler(){}
	
	@Override
	public void init(int maxLength, int type_size){}
	
	@Override
	public void genLabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv){}

	@Override
	public void writeLabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) {}

	@Override
	public int readLabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException{
		return 0;
	}

	@Override
	public void fillLabeledFeatureVector(DependencyInstance inst, ParserModel model) {}

	@Override
	public double getLabeledScore(IndexTuple itemId) {
		return 0;
	}

	@Override
	public void getType(DependencyInstance inst, IndexTuple itemId, ParserModel model) {
		itemId.type = 0;
	}

	@Override
	public void getTypes(int length, ParserModel model) {}

	@Override
	public TypeLabeler clone(int size, int type_size) {
		return new DefaultTypeLabeler();
	}
	
}
