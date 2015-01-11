package maxparser.parser.decoder.forest;

public class SiblingForest extends SingleEdgeForest{

	public SiblingForest(){}
	
	public SiblingForest(int end, int K) {
		this.end = end;
		this.K = K;
		chart = new ForestItem[(end + 1) * (end + 1) * 2 * 3][K];
		
		for(short s = 0; s <= end; ++s){
			for(short t = 0; t <= end; ++t){
				for(short dir = 0; dir < 2; ++dir){
					for(short comp = 0; comp < 3; ++comp){
						for(short k = 0; k < K; ++k){
							chart[getKey(s, t, dir, comp)][k] = new ForestItem(s, (short) -1, t, (short) -1, dir, comp, Double.NEGATIVE_INFINITY, null, null);
						}
					}
				}
			}
		}
	}
	
	@Override
	protected int getKey(short s, short t, short dir, short comp) {
		int key = s;
		key = key * (end + 1) + t;
		key = key * 2 + dir;
		key = key * 3 + comp;
		return key;
	}
	
	@Override
	protected String getDepString(ForestItem item) {
		if(item.left == null){
			return "";
		}
		
		if(item.comp == 1 || item.comp == 2){
			return (getDepString(item.left) + " " + getDepString(item.right)).trim();
		}
		else if(item.dir == 0){
			return ((getDepString(item.left) + " " + getDepString(item.right)).trim() 
					+ " " + item.s + "|" + item.t + ":" + item.type).trim();
		}
		else{
			return (item.t + "|" + item.s + ":" + item.type + " " 
					+ (getDepString(item.left) + " " + getDepString(item.right)).trim()).trim();
		}
	}
}
