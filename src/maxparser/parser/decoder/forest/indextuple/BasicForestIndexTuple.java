package maxparser.parser.decoder.forest.indextuple;

public class BasicForestIndexTuple extends ForestIndexTuple{
	public short s, t, dir, comp;
	
	public BasicForestIndexTuple(){}
	
	public BasicForestIndexTuple(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
	}
	
	public BasicForestIndexTuple setIndex(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
		return this;
	}
}
