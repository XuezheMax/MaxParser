package maxparser.parser.marginal.ioforest;

public class SiblingInOutForest extends SingleEdgeInOutForest{
	
	public SiblingInOutForest(){}
	
	public SiblingInOutForest(int size){
		beta = new double[size][size][2][3];
		alpha = new double[size][size][2][3];
	}

}
