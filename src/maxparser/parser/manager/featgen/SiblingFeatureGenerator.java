package maxparser.parser.manager.featgen;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.model.ParserModel;

public class SiblingFeatureGenerator extends SingleEdgeFeatureGenerator{
	
	@Override
	public void genUnlabeledFeatures(DependencyInstance inst, ParserModel model, FeatureVector fv) {
		super.genUnlabeledFeatures(inst, model, fv);
		int length = inst.length();
		
		// find all trip features
		for(int i = 0; i < length; ++i){
			if(inst.heads[i] == -1 && i != 0){
				continue;
			}
			
			// right children
			int prev = i;
			for(int j = i + 1; j < length; ++j){
				if(inst.heads[j] == i){
					addTripFeatures(inst, i, prev, j, model, fv);
					addSiblingFeatures(inst, prev, j, prev == i, model, fv);
					prev = j;
				}
			}
			
			//left children
			prev = i;
			for(int j = i - 1; j >= 0; --j){
				if(inst.heads[j] == i){
					addTripFeatures(inst, i, prev, j, model, fv);
					addSiblingFeatures(inst, prev, j, prev == i, model, fv);
					prev = j;
				}
			}
		}
	}
	
	public void addSiblingFeatures(DependencyInstance inst, int ch1, int ch2, boolean isST, ParserModel model, FeatureVector fv){
		// ch1 is always the closes to par
		String dir = ch1 > ch2 ? "RA" : "LA";
		
		int ch1_pos = isST ? model.getPOSIndex("STPOS") : inst.posIds[ch1];
		int ch2_pos = inst.posIds[ch2];
		int ch1_cpos = isST ? model.getCPOSIndex("STCPOS") : inst.cposIds[ch1];
		int ch2_cpos = inst.cposIds[ch2];
		int ch1_form = isST ? model.getFormIndex("STWRD") : inst.formIds[ch1];
		int ch2_form = inst.formIds[ch2];
		int ch1_lemma = isST ? model.getLemmaIndex("STLEM") : inst.lemmaIds[ch1];
		int ch2_lemma = inst.lemmaIds[ch2];
		
		//sibling pair feature
		addFeature("SIB_POS+POS", ch1_pos + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("SIB_CPOS+CPOS", ch1_cpos + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("SIB_FORM+FORM", ch1_form + "_" + ch2_form + "&" + dir, model, fv);
		addFeature("SIB_LEMMA+LEMMA", ch1_lemma + "_" + ch2_lemma + "&" + dir, model, fv);
		
		//sibling form & pos feature
		addFeature("SIB_FORM+POS", ch1_form + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("SIB_POS+FORM", ch1_pos + "_" + ch2_form + "&" + dir, model, fv);
		
		//sibling lemma & pos feature
		addFeature("SIB_LEMMA+POS", ch1_lemma + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("SIB_POS+LEMMA", ch1_pos + "_" + ch2_lemma + "&" + dir, model, fv);
		
		//sibling form & cpos feature
		addFeature("SIB_FORM+CPOS", ch1_form + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("SIB_CPOS+FORM", ch1_cpos + "_" + ch2_form + "&" + dir, model, fv);
		
		//sibling lemma & cpos feature
		addFeature("SIB_LEMMA+CPOS", ch1_lemma + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("SIB_CPOS+LEMMA", ch1_cpos + "_" + ch2_lemma + "&" + dir, model, fv);
		
		//features without direction
		//sibling pair feature
		addFeature("ASIB_POS+POS", ch1_pos + "_" + ch2_pos, model, fv);
		addFeature("ASIB_CPOS+CPOS", ch1_cpos + "_" + ch2_cpos, model, fv);
		addFeature("ASIB_FORM+FORM", ch1_form + "_" + ch2_form, model, fv);
		addFeature("ASIB_LEMMA+LEMMA", ch1_lemma + "_" + ch2_lemma, model, fv);
		
		//sibling form & pos feature
		addFeature("ASIB_FORM+POS", ch1_form + "_" + ch2_pos, model, fv);
		addFeature("ASIB_POS+FORM", ch1_pos + "_" + ch2_form, model, fv);
		
		//sibling lemma & pos feature
		addFeature("ASIB_LEMMA+POS", ch1_lemma + "_" + ch2_pos, model, fv);
		addFeature("ASIB_POS+LEMMA", ch1_pos + "_" + ch2_lemma, model, fv);
		
		//sibling form & cpos feature
		addFeature("ASIB_FORM+CPOS", ch1_form + "_" + ch2_cpos, model, fv);
		addFeature("ASIB_CPOS+FORM", ch1_cpos + "_" + ch2_form, model, fv);
		
		//sibling lemma & cpos feature
		addFeature("ASIB_LEMMA+CPOS", ch1_lemma + "_" + ch2_cpos, model, fv);
		addFeature("ASIB_CPOS+LEMMA", ch1_cpos + "_" + ch2_lemma, model, fv);
		
		//distance
		int dist = Math.abs(ch1 - ch2);
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
		
		//sibling distance features
		addFeature("SIB_DIST", distBool + "&" + dir, model, fv);
		addFeature("ASIB_DIST", distBool, model, fv);
		
		//sibling pair features with distance
		addFeature("SIB_POS+POS_DIST", ch1_pos + "_" + ch2_pos + "&" + distBool + "&" + dir, model, fv);
		addFeature("ASIB_POS+POS_DIST", ch1_pos + "_" + ch2_pos + "&" + distBool, model, fv);
		
		addFeature("SIB_CPOS+CPOS_DIST", ch1_cpos + "_" + ch2_cpos + "&" + distBool + "&" + dir, model, fv);
		addFeature("ASIB_CPOS+CPOS_DIST", ch1_cpos + "_" + ch2_cpos + "&" + distBool, model, fv);
	}
	
	public void addTripFeatures(DependencyInstance inst, int par, int ch1, int ch2, ParserModel model, FeatureVector fv){
		// ch1 is always the closest to par
		String dir = par > ch2 ? "RA" : "LA";
		
		//pos
		int par_pos = inst.posIds[par];
		int ch1_pos = ch1 == par ? model.getPOSIndex("STPOS") : inst.posIds[ch1];
		int ch2_pos = inst.posIds[ch2];
		
		addFeature("TRIP_POS", par_pos + "_" + ch1_pos + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("ATRIP_POS", par_pos + "_" + ch1_pos + "_" + ch2_pos, model, fv);
		
		//cpos
		int par_cpos = inst.cposIds[par];
		int ch1_cpos = ch1 == par ? model.getCPOSIndex("STCPOS") : inst.cposIds[ch1];
		int ch2_cpos = inst.cposIds[ch2];
		
		addFeature("TRIP_CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("ATRIP_CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos, model, fv);
		
		//form
		int par_form = inst.formIds[par];
		int ch1_form = ch1 == par ? model.getFormIndex("STWRD") : inst.formIds[ch1];
		int ch2_form = inst.formIds[ch2];
		
		addFeature("TRIP_FORM", par_form + "_" + ch1_form + "_" + ch2_form + "&" + dir, model, fv);
		addFeature("ATRIP_FORM", par_form + "_" + ch1_form + "_" + ch2_form, model, fv);
		
		//lemma
		int par_lemma = inst.lemmaIds[par];
		int ch1_lemma = ch1 == par ? model.getLemmaIndex("STLEM") : inst.lemmaIds[ch1];
		int ch2_lemma = inst.lemmaIds[ch2];
		
		addFeature("TRIP_LEMMA", par_lemma + "_" + ch1_lemma + "_" + ch2_lemma + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA", par_lemma + "_" + ch1_lemma + "_" + ch2_lemma, model, fv);
		
		//combination features
		//features with one form
		//par form
		addFeature("TRIP_FORM+POS", par_pos + "[" + par_form + "]" + "_" + ch1_pos + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+POS", par_pos + "[" + par_form + "]" + "_" + ch1_pos + "_" + ch2_pos, model, fv);
		
		addFeature("TRIP_FORM+CPOS", par_cpos + "[" + par_form + "]" + "_" + ch1_cpos + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+CPOS", par_cpos + "[" + par_form + "]" + "_" + ch1_cpos + "_" + ch2_cpos, model, fv);
		
		//ch1 form
		addFeature("TRIP_FORM+POS", par_pos + "_" + ch1_pos + "[" + ch1_form + "]" + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+POS", par_pos + "_" + ch1_pos + "[" + ch1_form + "]" + "_" + ch2_pos, model, fv);
		
		addFeature("TRIP_FORM+CPOS", par_cpos + "_" + ch1_cpos + "[" + ch1_form + "]" + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+CPOS", par_cpos + "_" + ch1_cpos + "[" + ch1_form + "]" + "_" + ch2_cpos, model, fv);
		
		//ch2 form
		addFeature("TRIP_FORM+POS", par_pos + "_" + ch1_pos + "_" + ch2_pos + "[" + ch2_form + "]" + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+POS", par_pos + "_" + ch1_pos + "_" + ch2_pos + "[" + ch2_form + "]", model, fv);
		
		addFeature("TRIP_FORM+CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos + "[" + ch2_form + "]" + "&" + dir, model, fv);
		addFeature("ATRIP_FORM+CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos + "[" + ch2_form + "]", model, fv);
		
		//features with one lemma
		//par lemma
		addFeature("TRIP_LEMMA+POS", par_pos + "[" + par_lemma + "]" + "_" + ch1_pos + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+POS", par_pos + "[" + par_lemma + "]" + "_" + ch1_pos + "_" + ch2_pos, model, fv);
				
		addFeature("TRIP_LEMMA+CPOS", par_cpos + "[" + par_lemma + "]" + "_" + ch1_cpos + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+CPOS", par_cpos + "[" + par_lemma + "]" + "_" + ch1_cpos + "_" + ch2_cpos, model, fv);
				
		//ch1 lemma
		addFeature("TRIP_LEMMA+POS", par_pos + "_" + ch1_pos + "[" + ch1_lemma + "]" + "_" + ch2_pos + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+POS", par_pos + "_" + ch1_pos + "[" + ch1_lemma + "]" + "_" + ch2_pos, model, fv);
				
		addFeature("TRIP_LEMMA+CPOS", par_cpos + "_" + ch1_cpos + "[" + ch1_lemma + "]" + "_" + ch2_cpos + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+CPOS", par_cpos + "_" + ch1_cpos + "[" + ch1_lemma + "]" + "_" + ch2_cpos, model, fv);
				
		//ch2 lemma
		addFeature("TRIP_LEMMA+POS", par_pos + "_" + ch1_pos + "_" + ch2_pos + "[" + ch2_lemma + "]" + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+POS", par_pos + "_" + ch1_pos + "_" + ch2_pos + "[" + ch2_lemma + "]", model, fv);
				
		addFeature("TRIP_LEMMA+CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos + "[" + ch2_lemma + "]" + "&" + dir, model, fv);
		addFeature("ATRIP_LEMMA+CPOS", par_cpos + "_" + ch1_cpos + "_" + ch2_cpos + "[" + ch2_lemma + "]", model, fv);
	}
}
