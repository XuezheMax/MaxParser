package maxparser;

import java.io.PrintWriter;
import java.io.IOException;

import maxparser.io.DependencyReader;
import maxparser.model.ParserModel;

public class Evaluator {
	public static double evaluate(String goldfile, String predfile, PrintWriter out, ParserModel model) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		int total = 0, corr = 0, corrLabel = 0;
		int numSent = 0, corrSent = 0, corrSentLabel = 0;
		
		int totalNonPunc = 0, corrNonPunc = 0, corrLabelNonPunc = 0;
		int corrSentNonPunc = 0, corrSentLabelNonPunc = 0;
		
		boolean labeled = model.labeled();
		
		DependencyReader goldReader = DependencyReader.createDependencyReader(model.getReader());
		DependencyReader predReader = DependencyReader.createDependencyReader(model.getReader());
		goldReader.startReading(goldfile);
		predReader.startReading(predfile);
		
		DependencyInstance goldInst = goldReader.getNext(model);
		DependencyInstance predInst = predReader.getNext(model);
		
		while(goldInst != null){
			boolean whole = true, wholeLabel = true;
			boolean wholeNonPunc = true, wholeLabelNonPunc = true;
			
			for(int i = 1; i < goldInst.length(); ++i){
				total++;
				if(predInst.heads[i] == goldInst.heads[i]){
					corr++;
					if(labeled){
						if(predInst.deprels[i].equals(goldInst.deprels[i])){
							corrLabel++;
						}
						else{
							wholeLabel = false;
						}
					}
				}
				else{
					whole = false;
					wholeLabel = false;
				}
				
				if(!model.isPunct(goldInst.postags[i])){
					totalNonPunc++;
					if(predInst.heads[i] == goldInst.heads[i]){
						corrNonPunc++;
						if(predInst.deprels[i].equals(goldInst.deprels[i])){
							corrLabelNonPunc++;
						}
						else{
							wholeLabelNonPunc = false;
						}
					}
					else{
						wholeNonPunc = false;
						wholeLabelNonPunc = false;
					}
				}
			}
			numSent++;
			if(whole){
				corrSent++;
			}
			if(wholeLabel){
				corrSentLabel++;
			}
			if(wholeNonPunc){
				corrSentNonPunc++;
			}
			if(wholeLabelNonPunc){
				corrSentLabelNonPunc++;
			}
			
			goldInst = goldReader.getNext(model);
			predInst = predReader.getNext(model);
		}
		
		goldReader.close();
		predReader.close();
		
		double UAS = ((double) corr) * 100 / total;
		double UCM = ((double)corrSent) *100 / numSent;
		double LAS = labeled ? ((double) corrLabel) * 100 / total : 0.0;
		double LCM = labeled ? ((double)corrSentLabel) *100 / numSent : 0.0;
		
		out.println("Tokens: " + total);
		out.println("Correct: " + corr);
		out.println(String.format("Unlabeled Accuracy: %.2f%%", UAS));
		out.println(String.format("Unlabeled Complete Correct: %.2f%%", UCM));
		if(labeled){
			out.println(String.format("Labeled Accuracy: %.2f%%", LAS));
			out.println(String.format("Labeled Complete Correct: %.2f%%", LCM));
		}
		out.println();
		
		double UASNonPunc = ((double) corrNonPunc) * 100 / totalNonPunc;
		double UCMNonPunc = ((double)corrSentNonPunc) *100 / numSent;
		double LASNonPunc = labeled ? ((double) corrLabelNonPunc) * 100 / totalNonPunc : 0.0;
		double LCMNonPunc = labeled ? ((double)corrSentLabelNonPunc) *100 / numSent : 0.0;
		
		out.println("Tokens (Non-Punct): " + totalNonPunc);
		out.println("Correct (Non-Punct): " + corrNonPunc);
		out.println(String.format("Unlabeled Accuracy (Non-Punct): %.2f%%", UASNonPunc));
		out.println(String.format("Unlabeled Complete Correct (Non-Punct): %.2f%%", UCMNonPunc));
		if(labeled){
			out.println(String.format("Labeled Accuracy (Non-Punct): %.2f%%", LASNonPunc));
			out.println(String.format("Labeled Complete Correct (Non-Punct): %.2f%%", LCMNonPunc));
		}
		
		return labeled ? LASNonPunc : UASNonPunc;
	}
}
