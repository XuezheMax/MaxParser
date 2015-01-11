package maxparser.parser.indextuple;

public class SingleEdgeIndexTuple extends IndexTuple{
	public int par;
	public int ch;
	
	public SingleEdgeIndexTuple(){
		super(0);
		par = 0;
		ch = 0;
	}
	
	public SingleEdgeIndexTuple(int par, int ch, int type){
		super(type);
		this.par = par;
		this.ch = ch;
	}
}
