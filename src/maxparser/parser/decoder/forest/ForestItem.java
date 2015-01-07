package maxparser.parser.decoder.forest;

public class ForestItem {
	public short s, r, t, dir, comp, type;
	public double score;
	
	ForestItem left;
	ForestItem right;
	
	public ForestItem(){}
	
	public ForestItem(short s, short r, short t, short type, short dir, short comp, double score,
			ForestItem left, ForestItem right){
		this.s = s;
		this.r = r;
		this.t = t;
		this.type = type;
		this.dir = dir;
		this.comp = comp;
		this.score = score;
		
		this.left = left;
		this.right = right;
	}
}
