package maxparser.parser.decoder.forest.indextuple;

public class SingleEdgeForestIndexTuple extends ForestIndexTuple{
	public short s, t, dir, comp;
	
	public SingleEdgeForestIndexTuple(){}
	
	public SingleEdgeForestIndexTuple(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
	}
	
	public SingleEdgeForestIndexTuple setIndex(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
		return this;
	}
}
