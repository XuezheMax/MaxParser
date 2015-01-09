package maxparser.parser.indextuple;

public class SingleEdgeIndexTuple extends IndexTuple{
	public int par;
	public int ch;
	public int type;
	
	public SingleEdgeIndexTuple(){
		par = 0;
		ch = 0;
		type = 0;
	}
	public SingleEdgeIndexTuple(int par, int ch, int type){
		this.par = par;
		this.ch = ch;
		this.type = type;
	}
}
