package maxparser.parser.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class SingleEdgeFeatureGenerator extends FeatureGenerator{

	@Override
	public void genUnlabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv) {
		for(int i = 0; i < inst.length(); ++i){
			if(inst.heads[i] == -1){
				continue;
			}
			addSingleEdgeFeatures(inst, inst.heads[i], i, model, fv);
		}
	}
	
	public void addSingleEdgeFeatures(DependencyInstance inst, int par, int ch, ParserModel model, FeatureVector fv){
		String att = par < ch ? "RA" : "LA";
		
		//distance
		int dist = Math.abs(par - ch);
		String distBool = null;
		if (dist > 10){
			distBool = "10";
		}
		else if (dist > 5){
			distBool = "5";
		}
		else{
			distBool = Integer.toString(dist - 1);
		}
		
		int first = Math.min(par, ch);
		int second = Math.max(par, ch);
		
		String attDist = "&" + att + "&" + distBool;
		
		//linear features
		addLinearFeatures("POS_LINEAR", inst.posIds, first, second, attDist, model, fv);
		addCorePOSFeatures("POS_CORE", inst.posIds, first, second, attDist, model, fv);
		
		addLinearFeatures("CPOS_LINEAR", inst.cposIds, first, second, attDist, model, fv);
		addCorePOSFeatures("CPOS_CORE", inst.cposIds, first, second, attDist, model, fv);
		
		addLinearFeatures("FORM_LINEAR", inst.formIds, first, second, attDist, model, fv);
		
		addLinearFeatures("LEMMA_LINEAR", inst.lemmaIds, first, second, attDist, model, fv);
		
		//two item features
		addTwoObsFeatures("FORM+POS_2ITEM", inst.formIds[par], inst.posIds[par], inst.formIds[ch], inst.posIds[ch], attDist, model, fv);
		addTwoObsFeatures("FORM+CPOS_2ITEM", inst.formIds[par], inst.cposIds[par], inst.formIds[ch], inst.cposIds[ch], attDist, model, fv);
		addTwoObsFeatures("LEMMA+POS_2ITEM", inst.lemmaIds[par], inst.posIds[par], inst.lemmaIds[ch], inst.posIds[ch], attDist, model, fv);
		addTwoObsFeatures("LEMMA+CPOS_2ITEM", inst.lemmaIds[par], inst.cposIds[par], inst.lemmaIds[ch], inst.cposIds[ch], attDist, model, fv);
		for(int i = 0; i < inst.morphIds[par].length; ++i){
			for(int j = 0; j < inst.morphIds[ch].length; ++j){
				addTwoObsFeatures("FF" + i + "*" + j, inst.formIds[par], inst.morphIds[par][i], inst.formIds[ch], inst.morphIds[ch][j], attDist, model, fv);
				addTwoObsFeatures("LF" + i + "*" + j, inst.lemmaIds[par], inst.morphIds[par][i], inst.lemmaIds[ch], inst.morphIds[ch][j], attDist, model, fv);
			}
		}
	}
	
	public void addLinearFeatures(String prefix, int[] obsVals, int first, int second, String attachDistance, ParserModel model, FeatureVector fv){
		String feat = obsVals[first] + "_" + obsVals[second];
		for(int i = first + 1; i < second; ++i){
			addFeature(prefix, feat + "_" + obsVals[i], model, fv);
			addFeature(prefix, feat + "_" +  obsVals[i] + attachDistance, model, fv);
		}
	}
	
	public void addCorePOSFeatures(String prefix, int[] obsVals, int first, int second, String attachDistance, ParserModel model, FeatureVector fv){
		int STR, MID, END;
		
		if(prefix.startsWith("POS")){
			STR = model.getPOSIndex("STR");
			MID = model.getPOSIndex("MID");
			END = model.getPOSIndex("END");
		}
		else{
			STR = model.getCPOSIndex("STR");
			MID = model.getCPOSIndex("MID");
			END = model.getCPOSIndex("END");
		}
		
		int leftOf1 = first > 0 ? obsVals[first - 1] : STR;
		int rightOf1 = first < second - 1 ? obsVals[first + 1] : MID;
		int leftOf2 = second > first + 1 ? obsVals[second - 1] : MID;
		int rightOf2 = second < obsVals.length - 1 ? obsVals[second + 1] : END;
		
		int one = obsVals[first];
		int two = obsVals[second];
	 
		// feature posL-1 posL posR posR+1
		addFeature(prefix + "A1", leftOf1 + "_" + one + "_" + two + attachDistance, model, fv);
		
		StringBuilder feat = new StringBuilder(leftOf1 + "_" + one + "_" + two);
		addFeature(prefix + "A1", feat.toString(), model, fv);
		feat.append("_" + rightOf2);
		addFeature(prefix + "A1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "A1", feat.toString(), model, fv);
		
		feat = new StringBuilder(leftOf1 + "_" + two + "_" + rightOf2);
		addFeature(prefix + "A2", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "A2", feat.toString(), model, fv);
		
		feat = new StringBuilder(leftOf1 + "_" + one + "_" + rightOf2);
		addFeature(prefix + "A3", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "A3", feat.toString(), model, fv);
		
		feat = new StringBuilder(one + "_" + two + "_" + rightOf2);
		addFeature(prefix + "A4", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "A4", feat.toString(), model, fv);
		
		// feature posL posL+1 posR-1 posR
		addFeature(prefix + "B1", one + "_" + rightOf1 + "_" + leftOf2 + attachDistance, model, fv);
		
		feat = new StringBuilder(one + "_" + rightOf1 + "_" + leftOf2);
		addFeature(prefix + "B1", feat.toString(), model, fv);
		feat.append("_" + two);
		addFeature(prefix + "B1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "B1", feat.toString(), model, fv);
		
		feat = new StringBuilder(one + "_" + rightOf1 + "_" + two);
		addFeature(prefix + "B2", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "B2", feat.toString(), model, fv);
		
		feat = new StringBuilder(one + "_" + leftOf2 + "_" + two);
		addFeature(prefix + "B3", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "B3", feat.toString(), model, fv);
		
		feat = new StringBuilder(rightOf1 + "_" + leftOf2 + "_" + two);
		addFeature(prefix + "B4", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "B4", feat.toString(), model, fv);
		
		// feature posL-1 posL posR-1 posR
		feat = new StringBuilder(leftOf1 + "_" + one + "_" + leftOf2 + "_" + two);
		addFeature(prefix + "C1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "C1", feat.toString(), model, fv);
		
		feat = new StringBuilder(one + "_" + rightOf1 + "_" + two + "_" + rightOf2);
		addFeature(prefix + "C2", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "C2", feat.toString(), model, fv);
	}
	
	/**
	 * Add features for two items, each with two observations, e.g. head, head
	 * pos, child, and child pos.
	 * 
	 * The use of StringBuilders is not yet as efficient as it could be, but
	 * this is a start. (And it abstracts the logic so we can add other features
	 * more easily based on other items and observations.)
 **/
	public void addTwoObsFeatures(String prefix, int obs1F1, int obs1F2, int obs2F1, int obs2F2, String attachDistance, ParserModel model, FeatureVector fv){
		StringBuilder feat = new StringBuilder(String.valueOf(obs1F1));
		addFeature(prefix + "1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "1", feat.toString(), model, fv);
		
		feat = new StringBuilder(obs1F1 + "_" + obs1F2);
		addFeature(prefix + "1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "1", feat.toString(), model, fv);
		
		feat = new StringBuilder(obs1F1 + "_" + obs1F2 + "_" + obs2F2);
		addFeature(prefix + "1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "1", feat.toString(), model, fv);
		
		feat = new StringBuilder(obs1F1 + "_" + obs1F2 + "_" + obs2F2 + "_" + obs2F1);
		addFeature(prefix + "1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "1", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(obs1F1 + "_" + obs2F1);
		addFeature(prefix + "2", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "2", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(obs1F1 + "_" + obs2F2);
		addFeature(prefix + "3", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "3", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(obs1F2 + "_" + obs2F1);
		addFeature(prefix + "4", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "4", feat.toString(), model, fv);
		
		feat = new StringBuilder(obs1F2 + "_" + obs2F1 + "_" + obs2F2);
		addFeature(prefix + "4", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "4", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(obs1F2 + "_" + obs2F2);
		addFeature(prefix + "5", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "5", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(obs2F1 + "_" + obs2F2);
		addFeature(prefix + "6", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "6", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(String.valueOf(obs1F2));
		addFeature(prefix + "7", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "7", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(String.valueOf(obs2F1));
		addFeature(prefix + "8", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "8", feat.toString(), model, fv);
		
		//
		feat = new StringBuilder(String.valueOf(obs2F2));
		addFeature(prefix + "9", feat.toString(), model, fv);
		feat.append('&').append(attachDistance);
		addFeature(prefix + "9", feat.toString(), model, fv);
	}
}
