package maxparser.parser.indextuple;

public class SecondOrderSiblingIndexTuple extends FirstOrderIndexTuple{
	public int ch1;
	public int type1;
	
	public SecondOrderSiblingIndexTuple(int par, int ch1, int ch, int type1, int type){
		super(par, ch, type);
		this.ch1 = ch1;
		this.type1 = type1;
	}
}
