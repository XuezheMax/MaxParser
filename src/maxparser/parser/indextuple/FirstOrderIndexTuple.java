package maxparser.parser.indextuple;

public class FirstOrderIndexTuple extends IndexTuple{
	public int par;
	public int ch;
	public int type;
	
	public FirstOrderIndexTuple(int par, int ch, int type){
		this.par = par;
		this.ch = ch;
		this.type = type;
	}
}
