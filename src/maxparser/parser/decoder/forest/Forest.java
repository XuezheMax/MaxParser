package maxparser.parser.decoder.forest;

import maxparser.model.ParserModel;
import maxparser.parser.decoder.forest.indextuple.ForestIndexTuple;
import maxparser.parser.manager.Manager;
import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;

public abstract class Forest {
	protected int end, K;
	
	protected abstract int getKey(ForestIndexTuple forestIndex);
	
	public abstract ForestItem[] getItems(ForestIndexTuple forestIndex);
	
	public abstract boolean addItem(ForestIndexTuple forestIndex, short r, short type, double score, ForestItem left, ForestItem right);
	
	public abstract Pair<FeatureVector, String>[] getBestParses(DependencyInstance inst, Manager manager, ParserModel model);
	
	protected FeatureVector getFeatureVector(DependencyInstance inst, Manager manager, ParserModel model, String depStr){
		Pair<int[], int[]> p = manager.getHeadsTypesfromTreeString(depStr);
		
		int[] heads_tmp = inst.heads;
		int[] typeIds_tmp = inst.deprelIds;
		
		inst.heads = p.first;
		inst.deprelIds = p.second;
		
		FeatureVector fv = manager.createFeatureVector(inst, model);
		
		inst.heads = heads_tmp;
		inst.deprelIds = typeIds_tmp;
		
		return fv;
	}
	
	protected abstract String getDepString(ForestItem item);
	
	public int[][] getKBestPairs(ForestItem[] item1, ForestItem[] item2){
		boolean[][] beenPushed = new boolean[K][K];
		int[][] result = new int[K][2];
		for(int i = 0; i < K; i++){
			result[i][0] = -1;
			result[i][1] = -1;
		}
		
		BinaryHeap heap = new BinaryHeap(K + 1);
		int n = 0;
		ValueIndexPair vip = new ValueIndexPair(item1[0].score + item2[0].score, 0, 0);
		
		heap.add(vip);
		beenPushed[0][0] = true;
		
		while(n < K){
			vip = heap.removeMax();
			
			if(vip.val == Double.NEGATIVE_INFINITY){
				break;
			}
			
			result[n][0] = vip.i1;
			result[n][1] = vip.i2;
			
			++n;
			
			if(n >= K){
				break;
			}
			
			if(!beenPushed[vip.i1 + 1][vip.i2]){
				heap.add(new ValueIndexPair(item1[vip.i1 + 1].score + item2[vip.i2].score, vip.i1 + 1, vip.i2));
				beenPushed[vip.i1 + 1][vip.i2] = true;
			}
			
			if(!beenPushed[vip.i1][vip.i2 + 1]){
				heap.add(new ValueIndexPair(item1[vip.i1].score + item2[vip.i2 + 1].score, vip.i1, vip.i2 + 1));
				beenPushed[vip.i1][vip.i2 + 1] = true;
			}
		}
		return result;
	}
}

class ValueIndexPair{
	public double val;
	
	public int i1, i2;
	
	public ValueIndexPair(double val, int i1, int i2){
		this.val = val;
		this.i1 = i1;
		this.i2 = i2;
	}
	
	public int compareTo(ValueIndexPair other){
		if(this.val < other.val){
			return -1;
		}
		else if(this.val > other.val){
			return 1;
		}
		else{
			return 0;
		}
	}
}

//Max Heap
//We know that never more than K elements on Heap

class BinaryHeap{
	private int DEFAULT_CAPACITY;

	private int currentSize;

	private ValueIndexPair[] theArray;
	
	public BinaryHeap(int def_cap){
		DEFAULT_CAPACITY = def_cap;
		theArray = new ValueIndexPair[DEFAULT_CAPACITY + 1];
		theArray[0] = new ValueIndexPair(Double.POSITIVE_INFINITY, -1, -1);
		currentSize = 0;
	}
	
	private int parent(int id){
		return id / 2;
	}
	
	private int leftChild(int id){
		return id * 2;
	}
	
	private int rightChild(int id){
		return id * 2 + 1;
	}
	
	public void add(ValueIndexPair e){
		int wh = currentSize + 1;
		while(e.compareTo(theArray[parent(wh)]) > 0){
			theArray[wh] = theArray[parent(wh)];
			wh = parent(wh);
		}
		theArray[wh] = e;
		currentSize++;
	}
	
	public ValueIndexPair removeMax(){
		ValueIndexPair max = theArray[1];
		theArray[1] = theArray[currentSize--];
		
		boolean switched = true;
		int par = 1;
		while(switched && par < currentSize){
			switched = false;
			int leftC = leftChild(par);
			int rightC = rightChild(par);
			
			if(leftC <= currentSize){
				int largerC = leftC;
				if((rightC <= currentSize) && (theArray[rightC].compareTo(theArray[leftC]) > 0)){
					largerC = rightC;
				}
				
				if(theArray[largerC].compareTo(theArray[par]) > 0){
					ValueIndexPair temp = theArray[largerC];
					theArray[largerC] = theArray[par];
					theArray[par] = temp;
					par = largerC;
					switched = true;
				}
			}
		}
		
		return max;
	}
}