package maxparser.parser.decoder.forest.indextuple;

public class FirstOrderForestIndexTuple extends ForestIndexTuple{
	public short s, r, t, dir, comp;
	
	public FirstOrderForestIndexTuple(short s, short r, short t, short dir, short comp){
		this.s = s;
		this.r = r;
		this.t = t;
		this.dir = dir;
		this.comp = comp;
	}
}
