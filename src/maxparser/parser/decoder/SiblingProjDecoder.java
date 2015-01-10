package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;

public class SiblingProjDecoder extends SingleEdgeProjDecoder{
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		int length = inst.length();
		short zero = (short) 0;
		short one = (short) 1;
		short minusOne = (short) -1;
		
		manager.getTypes(length);
		
		return null;
	}
}
