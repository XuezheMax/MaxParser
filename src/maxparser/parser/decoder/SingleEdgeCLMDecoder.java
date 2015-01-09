package maxparser.parser.decoder;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;

public class SingleEdgeCLMDecoder extends SingleEdgeProjDecoder{
	@Override
	public Pair<FeatureVector, String>[] decode(Manager manager, DependencyInstance inst, int K, ParserModel model) {
		Pair<FeatureVector, String>[] d = super.decode(manager, inst, K, model);
		
		return d;
	}
	
	protected boolean[][] calcChildren(int[] par, int length){
		boolean[][] isChild = new boolean[length][length];
		for(int i = 0; i < length; ++i){
			int pa = par[i];
			while(pa != -1){
				isChild[pa][i] = true;
				pa = par[pa];
			}
		}
		return isChild;
	}
}
