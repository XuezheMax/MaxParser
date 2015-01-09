package maxparser.io;

import java.io.IOException;
import java.util.ArrayList;

import maxparser.DependencyInstance;
import maxparser.model.ParserModel;

public class CONLLReader extends DependencyReader{
	
	public CONLLReader(){}
	
	@Override
	public DependencyInstance getNext(ParserModel model) throws IOException{
		ArrayList<String[]> lineList = new ArrayList<String[]>();

		String line = inputReader.readLine();
		while (line != null && line.length() != 0) {
			lineList.add(line.split("\t"));
			line = inputReader.readLine();
		}

		int length = lineList.size();

		if (length == 0) {
			return null;
		}
		
		String[] forms = new String[length + 1];
		int[] formIds = new int[length + 1];
		forms[0] = "<ROOT-FORM>";
		formIds[0] = model.getFormIndex(forms[0]);
		
		String[] lemmas = new String[length + 1];
		int[] lemmaIds = new int[length + 1];
		lemmas[0] = "<ROOT-LEMMA>";
		lemmaIds[0] = model.getLemmaIndex(lemmas[0]);
		
		String[] cpos = new String[length + 1];
		int[] cposIds = new int[length + 1];
		cpos[0] = "<ROOT-CPOS>";
		cposIds[0] = model.getCPOSIndex(cpos[0]);
		
		String[] pos = new String[length + 1];
		int[] posIds = new int[length + 1];
		pos[0] = "<ROOT-POS>";
		posIds[0] = model.getPOSIndex(pos[0]);
		
		String[][] morphs = new String[length + 1][];
		int[][] morphIds = new int[length + 1][];
		
		String[] deprels = new String[length + 1];
		int[] deprelIds = new int[length + 1];
		deprels[0] = "<no-type>";
		deprelIds[0] = model.getTypeIndex(deprels[0]);
		
		int[] heads = new int[length + 1];
		heads[0] = -1;
		
		int i = 1;
		for(String[] info : lineList){
			forms[i] = info[1];
			formIds[i] = model.getFormIndex(normalize(forms[i]));
			
			lemmas[i] = info[2];
			lemmaIds[i] = model.getLemmaIndex(normalize(lemmas[i]));
			
			cpos[i] = info[3];
			cposIds[i] = model.getCPOSIndex(cpos[i]);
			
			pos[i] = info[4];
			posIds[i] = model.getPOSIndex(pos[i]);
			
			morphs[i] = info[5].split("\\|");
			morphIds[i] = new int[morphs[i].length];
			int j = 0;
			for(String morph : morphs[i]){
				morphIds[i][j++] = model.getMorphIndex(morph);
			}
			
			heads[i] = Integer.parseInt(info[6]);
			
			deprels[i] = model.labeled() ? info[7] : "<no-type>";
			deprelIds[i] = model.getTypeIndex(deprels[i]);
			
			i++;
		}
		
		morphs[0] = new String[morphs[1].length];
		morphIds[0] = new int[morphs[0].length];
		for(int j = 0; j < morphs[0].length; ++j){
			morphs[0][j] = "<root-feat>" + j;
			morphIds[0][j] = model.getMorphIndex(morphs[0][j]);
		}
		
		return new DependencyInstance(forms, formIds, lemmas, lemmaIds, cpos, cposIds, pos, posIds, morphs, morphIds, heads, deprels, deprelIds);
	}
}
