package maxparser.parser.decoder.forest.indextuple;

public class FirstOrderForestIndexTuple extends ForestIndexTuple{
	public short s, t, dir, comp;
	
	public FirstOrderForestIndexTuple(){}
	
	public FirstOrderForestIndexTuple(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
	}
	
	public FirstOrderForestIndexTuple setIndex(short s, short t, short dir, short comp){
		this.s = s;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
		return this;
	}
}
