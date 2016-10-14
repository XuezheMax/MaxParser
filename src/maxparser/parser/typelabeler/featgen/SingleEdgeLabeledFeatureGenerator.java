package maxparser.parser.typelabeler.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class SingleEdgeLabeledFeatureGenerator extends SimpleLabeledFeatureGenerator{

	public SingleEdgeLabeledFeatureGenerator(){}

	public void addSingleEdgeLabeledFeatures(DependencyInstance inst, int par, int ch, int type, ParserModel model, FeatureVector fv){
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
		
		String attDist = "&" + att + "&" + distBool;
		
		//form
		addFeature("LWPC", type + "&" + inst.formIds[par] + "_" + inst.formIds[ch] + attDist, model, fv);
		addFeature("LWPC", type + "&" + inst.formIds[par] + "_" + inst.formIds[ch], model, fv);
		
		//lemma
		addFeature("LLPC", type + "&" + inst.lemmaIds[par] + "_" + inst.lemmaIds[ch] + attDist, model, fv);
		addFeature("LLPC", type + "&" + inst.lemmaIds[par] + "_" + inst.lemmaIds[ch], model, fv);
		
		//pos
		addFeature("LPPC", type + "&" + inst.posIds[par] + "_" + inst.posIds[ch] + attDist, model, fv);
		addFeature("LPPC", type + "&" + inst.posIds[par] + "_" + inst.posIds[ch], model, fv);
		
		//cpos
		addFeature("LCPC", type + "&" + inst.cposIds[par] + "_" + inst.cposIds[ch] + attDist, model, fv);
		addFeature("LCPC", type + "&" + inst.cposIds[par] + "_" + inst.cposIds[ch], model, fv);
		
		//form+pos
		addTwoObsLabeledFeatures("LWP", type, inst.formIds[par], inst.posIds[par], inst.formIds[ch], inst.posIds[ch], attDist, model, fv);
		
		//lemma+pos
		addTwoObsLabeledFeatures("LLP", type, inst.lemmaIds[par], inst.posIds[par], inst.lemmaIds[ch], inst.posIds[ch], attDist, model, fv);
		
		//form+cpos
		addTwoObsLabeledFeatures("LWC", type, inst.formIds[par], inst.cposIds[par], inst.formIds[ch], inst.cposIds[ch], attDist, model, fv);
		
		//lemma+cpos
		addTwoObsLabeledFeatures("LLC", type, inst.lemmaIds[par], inst.cposIds[par], inst.lemmaIds[ch], inst.cposIds[ch], attDist, model, fv);
	}
	
	public void addTwoObsLabeledFeatures(String prefix, int type, int obs1F1, int obs1F2, int obs2F1, int obs2F2, String attachDistance, ParserModel model, FeatureVector fv){
		StringBuilder feat = new StringBuilder(type + "&" + obs1F1 + "_" + obs1F2);
		addFeature(prefix + "1", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "1", feat.toString(), model, fv);
		
		feat = new StringBuilder(type + "&" + obs1F1 + "_" + obs2F2);
		addFeature(prefix + "2", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "2", feat.toString(), model, fv);
		
		feat = new StringBuilder(type + "&" + obs1F2 + "_" + obs2F1);
		addFeature(prefix + "3", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "3", feat.toString(), model, fv);
		
		feat = new StringBuilder(type + "&" + obs2F1 + "_" + obs2F2);
		addFeature(prefix + "4", feat.toString(), model, fv);
		feat.append(attachDistance);
		addFeature(prefix + "4", feat.toString(), model, fv);
	}
}
