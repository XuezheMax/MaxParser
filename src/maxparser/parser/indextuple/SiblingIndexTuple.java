package maxparser.parser.indextuple;

public class SiblingIndexTuple extends SingleEdgeIndexTuple{
	public int ch1;
	public int type1;
	
	public SiblingIndexTuple(){
		ch1 = 0;
		type1 = 0;
	}
	
	public SiblingIndexTuple(int par, int ch1, int ch, int type1, int type){
		super(par, ch, type);
		this.ch1 = ch1;
		this.type1 = type1;
	}
}
