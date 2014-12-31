package maxparser.io;

import java.io.IOException;

import maxparser.DependencyInstance;

public class CONLLWriter extends DependencyWriter {
	public CONLLWriter(){}
	
	@Override
	public void write(DependencyInstance instance, int[] heads, String[] types) throws IOException{
		for(int i = 1; i < instance.length(); ++i){
			writer.write(i + "\t");
			writer.write(instance.forms[i] + "\t");
			writer.write(instance.lemmas[i] + "\t");
			writer.write(instance.cpostags[i] + "\t");
			writer.write(instance.postags[i] + "\t");
			for(int j = 0; j < instance.morphs[i].length; ++j){
				if(j != 0){
					writer.write("|");
				}
				writer.write(instance.morphs[i][j]);
			}
			writer.write("\t" + heads[i] + "\t" + types[i] + "\t_\t_");
			writer.newLine();
		}
		writer.newLine();
	}
}
