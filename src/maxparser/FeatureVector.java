package maxparser;

import gnu.trove.list.linked.TLinkedList;
import gnu.trove.map.hash.TIntDoubleHashMap;

@SuppressWarnings("rawtypes")
public class FeatureVector extends TLinkedList {
	private FeatureVector subfv1 = null;

	private FeatureVector subfv2 = null;

	private boolean negateSecondSubFV = false;
	
	public FeatureVector(){}
	
	public FeatureVector(FeatureVector fv1, FeatureVector fv2, boolean negSecond){
		subfv1 = fv1;
		subfv2 = fv2;
		negateSecondSubFV = negSecond;
	}
	
	@SuppressWarnings("unchecked")
	public FeatureVector(int[] keys){
		for(int i = 0; i < keys.length; ++i){
			this.add(new Feature(keys[i], 1.0));		
		}
	}
	
	@SuppressWarnings("unchecked")
	public void add(int index, double value){
		this.add(new Feature(index, value));
	}

	public int[] keys(){
		int size = this.size();
		int[] keys = new int[size];
		int i = 0;
		for(Object b : this){
			Feature f = (Feature)(b);
			keys[i++] = f.index;
		}
		return keys;
	}
	
	public FeatureVector getDistVector(FeatureVector fv2){
		return new FeatureVector(this, fv2, true);
	}
	
	public final double getScore(double[] params){
		double score = 0.0;
		if(subfv1 != null){
			score += subfv1.getScore(params);
		}
		
		if(subfv2 != null){
			if(negateSecondSubFV){
				score -= subfv2.getScore(params);
			}
			else{
				score += subfv2.getScore(params);
			}
		}
		
		for(Object b : this){
			Feature f = (Feature)(b);
			score += params[f.index] * f.value;
		}
		return score;
	}
	
	public void update(double[] parameters, double[] total, double alpha_k, double upd){
		for(Object b : subfv1){
			Feature f = (Feature)(b);
			parameters[f.index] += alpha_k * f.value;
			total[f.index] += upd * alpha_k * f.value;
		}
		
		for(Object b : subfv2){
			Feature f = (Feature)(b);
			parameters[f.index] -= alpha_k * f.value;
			total[f.index] -= upd * alpha_k * f.value;
		}
	}
	
	public void update(double[] parameters, double alpha_k){
		for(Object b : subfv1){
			Feature f = (Feature)(b);
			parameters[f.index] += alpha_k * f.value;
		}
		
		for(Object b : subfv2){
			Feature f = (Feature)(b);
			parameters[f.index] -= alpha_k * f.value;
		}
	}
	
	public double dotProduct(FeatureVector fv2){
		TIntDoubleHashMap hm1 = new TIntDoubleHashMap(this.size());
		addFeaturesToMap(hm1, false);
		hm1.compact();
		
		TIntDoubleHashMap hm2 = new TIntDoubleHashMap(fv2.size());
		fv2.addFeaturesToMap(hm2, false);
		hm2.compact();
		
		int[] keys = hm1.keys();
		
		double score = 0.0;
		for(int i = 0; i < keys.length; ++i){
			score += hm1.get(keys[i]) * hm2.get(keys[i]);
		}
		return score;
	}
	
	public void addFeaturesToMap(TIntDoubleHashMap map, boolean negate){
		if(subfv1 != null){
			subfv1.addFeaturesToMap(map, negate);
			if(subfv2 != null){
				subfv2.addFeaturesToMap(map, negate ^ negateSecondSubFV);
			}
		}
		for(Object b : this){
			Feature f = (Feature)(b);
			if(negate){
				if(!map.adjustValue(f.index, -f.value)){
					map.put(f.index, -f.value);
				}
			}
			else{
				if(!map.adjustValue(f.index, f.value)){
					map.put(f.index, f.value);
				}
			}
		}
	}
}
