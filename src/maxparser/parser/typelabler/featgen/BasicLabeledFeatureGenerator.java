package maxparser.parser.typelabler.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class BasicLabeledFeatureGenerator extends LabeledFeatureGenerator{

	public BasicLabeledFeatureGenerator(){}
	
	public void addChildSingleLabelFeatures(DependencyInstance inst, int ch, int type, boolean attR, ParserModel model, FeatureVector fv){
		String att = attR ? "RA&C" : "LA&C";
		
		int form = inst.formIds[ch];
		int lemma = inst.lemmaIds[ch];
		int pos = inst.posIds[ch];
		int cpos = inst.cposIds[ch];
		
		//pos+1, pos-1
		int posm1 = ch > 0 ? inst.posIds[ch - 1] : model.getPOSIndex("STR");
		int posp1 = ch < inst.length() - 1 ? inst.posIds[ch + 1] : model.getPOSIndex("END");
		
		//cpos+1, cpos-1
		int cposm1 = ch > 0 ? inst.cposIds[ch - 1] : model.getCPOSIndex("STR");
		int cposp1 = ch < inst.length() - 1 ? inst.cposIds[ch + 1] : model.getCPOSIndex("END");
		
		addFeature("NTS1", type + "&" + att, model, fv);
		addFeature("NTS1", Integer.toString(type), model, fv);
		
		//form + pos
		addFeature("NTWP", type + "&" + form + "_" + pos + "&" + att, model, fv);
		addFeature("NTWP", type + "&" + form + "_" + pos, model, fv);
		
		//lemma + pos
		addFeature("NTLP", type + "&" + lemma + "_" + pos + "&" + att, model, fv);
		addFeature("NTLP", type + "&" + lemma + "_" + pos, model, fv);
		
		//form + cpos
		addFeature("NTWC", type + "&" + form + "_" + cpos + "&" + att, model, fv);
		addFeature("NTWC", type + "&" + form + "_" + cpos, model, fv);
		
		//lemma + cpos
		addFeature("NTLC", type + "&" + lemma + "_" + cpos + "&" + att, model, fv);
		addFeature("NTLC", type + "&" + lemma + "_" + cpos, model, fv);
		
		//pos
		addFeature("NTPOS", type + "&" + pos + "&" + att, model, fv);
		addFeature("NTPOS", type + "&" + pos, model, fv);
		
		//cpos
		addFeature("NTCPOS", type + "&" + cpos + "&" + att, model, fv);
		addFeature("NTCPOS", type + "&" + cpos, model, fv);
		
		//pos-1, pos, pos+1
		addFeature("NTPA", type + "&" + posm1 + "_" + pos + "&" + att, model, fv);
		addFeature("NTPA", type + "&" + posm1 + "_" + pos, model, fv);
		
		addFeature("NTPB", type + "&" + pos + "_" + posp1 + "&" + att, model, fv);
		addFeature("NTPB", type + "&" + pos + "_" + posp1, model, fv);
		
		addFeature("NTPC", type + "&" + posm1 + "_" + pos + "_" + posp1 + "&" + att, model, fv);
		addFeature("NTPC", type + "&" + posm1 + "_" + pos + "_" + posp1, model, fv);
		
		//cpos-1, cpos, cpos+1
		addFeature("NTCA", type + "&" + cposm1 + "_" + cpos + "&" + att, model, fv);
		addFeature("NTCA", type + "&" + cposm1 + "_" + cpos, model, fv);
		
		addFeature("NTCB", type + "&" + cpos + "_" + cposp1 + "&" + att, model, fv);
		addFeature("NTCB", type + "&" + cpos + "_" + cposp1, model, fv);
		
		addFeature("NTCC", type + "&" + cposm1 + "_" + cpos + "_" + cposp1 + "&" + att, model, fv);
		addFeature("NTCC", type + "&" + cposm1 + "_" + cpos + "_" + cposp1, model, fv);
		
		//form
		addFeature("NTFORM", type + "&" + form + "&" + att, model, fv);
		addFeature("NTFORM", type + "&" + form, model, fv);
		
		//lemma
		addFeature("NTLEMMA", type + "&" + lemma + "&" + att, model, fv);
		addFeature("NTLEMMA", type + "&" + lemma, model, fv);
	}
	
	public void addParentSingleLabelFeatures(DependencyInstance inst, int par, int type, boolean attR, ParserModel model, FeatureVector fv){
		String att = attR ? "RA&P" : "LA&P";
		
		int form = inst.formIds[par];
		int lemma = inst.lemmaIds[par];
		int pos = inst.posIds[par];
		int cpos = inst.cposIds[par];
		
		//pos+1, pos-1
		int posm1 = par > 0 ? inst.posIds[par - 1] : model.getPOSIndex("STR");
		int posp1 = par < inst.length() - 1 ? inst.posIds[par + 1] : model.getPOSIndex("END");
		
		//cpos+1, cpos-1
		int cposm1 = par > 0 ? inst.cposIds[par - 1] : model.getCPOSIndex("STR");
		int cposp1 = par < inst.length() - 1 ? inst.cposIds[par + 1] : model.getCPOSIndex("END");
		
		addFeature("NTS1", type + "&" + att, model, fv);
		addFeature("NTS1", Integer.toString(type), model, fv);
		
		//form + pos
		addFeature("NTWP", type + "&" + form + "_" + pos + "&" + att, model, fv);
		addFeature("NTWP", type + "&" + form + "_" + pos, model, fv);
		
		//lemma + pos
		addFeature("NTLP", type + "&" + lemma + "_" + pos + "&" + att, model, fv);
		addFeature("NTLP", type + "&" + lemma + "_" + pos, model, fv);
		
		//form + cpos
		addFeature("NTWC", type + "&" + form + "_" + cpos + "&" + att, model, fv);
		addFeature("NTWC", type + "&" + form + "_" + cpos, model, fv);
		
		//lemma + cpos
		addFeature("NTLC", type + "&" + lemma + "_" + cpos + "&" + att, model, fv);
		addFeature("NTLC", type + "&" + lemma + "_" + cpos, model, fv);
		
		//pos
		addFeature("NTPOS", type + "&" + pos + "&" + att, model, fv);
		addFeature("NTPOS", type + "&" + pos, model, fv);
		
		//cpos
		addFeature("NTCPOS", type + "&" + cpos + "&" + att, model, fv);
		addFeature("NTCPOS", type + "&" + cpos, model, fv);
		
		//pos-1, pos, pos+1
		addFeature("NTPA", type + "&" + posm1 + "_" + pos + "&" + att, model, fv);
		addFeature("NTPA", type + "&" + posm1 + "_" + pos, model, fv);
		
		addFeature("NTPB", type + "&" + pos + "_" + posp1 + "&" + att, model, fv);
		addFeature("NTPB", type + "&" + pos + "_" + posp1, model, fv);
		
		addFeature("NTPC", type + "&" + posm1 + "_" + pos + "_" + posp1 + "&" + att, model, fv);
		addFeature("NTPC", type + "&" + posm1 + "_" + pos + "_" + posp1, model, fv);
		
		//cpos-1, cpos, cpos+1
		addFeature("NTCA", type + "&" + cposm1 + "_" + cpos + "&" + att, model, fv);
		addFeature("NTCA", type + "&" + cposm1 + "_" + cpos, model, fv);
		
		addFeature("NTCB", type + "&" + cpos + "_" + cposp1 + "&" + att, model, fv);
		addFeature("NTCB", type + "&" + cpos + "_" + cposp1, model, fv);
		
		addFeature("NTCC", type + "&" + cposm1 + "_" + cpos + "_" + cposp1 + "&" + att, model, fv);
		addFeature("NTCC", type + "&" + cposm1 + "_" + cpos + "_" + cposp1, model, fv);
		
		//form
		addFeature("NTFORM", type + "&" + form + "&" + att, model, fv);
		addFeature("NTFORM", type + "&" + form, model, fv);
		
		//lemma
		addFeature("NTLEMMA", type + "&" + lemma + "&" + att, model, fv);
		addFeature("NTLEMMA", type + "&" + lemma, model, fv);
	}

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
