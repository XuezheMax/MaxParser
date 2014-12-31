package maxparser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class DependencyInstance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private FeatureVector fv = null;
	
	private String actParseTree = null;
	
	// FORM: the forms - usually words, like "thought"
	public String[] forms = null;
	public int[] formIds = null;

	// LEMMA: the lemmas, or stems, e.g. "think"
	public String[] lemmas = null;
	public int[] lemmaIds = null;

	// COURSE-POS: the course part-of-speech tags, e.g."V"
	public String[] cpostags = null;
	public int[] cposIds = null;

	// FINE-POS: the fine-grained part-of-speech tags, e.g."VBD"
	public String[] postags = null;
	public int[] posIds = null;

	// FEATURES: some features associated with the elements separated by "|", e.g. "PAST|3P"
	public String[][] morphs = null;
	public int[][] morphIds = null;

	// HEAD: the IDs of the heads for each element
	public int[] heads = null;

	// DEPREL: the dependency relations, e.g. "SUBJ"
	public String[] deprels = null;
	public int[] deprelIds = null;
	
	public DependencyInstance(){}
	
	public DependencyInstance(String[] forms, int[] formIds, String[] lemmas, int[] lemmaIds, 
			String[] cpostags, int[] cposIds, String[] postags, int[] posIds, 
			String[][] morphs, int[][] morphIds, int[] heads, String[] deprels, int[] deprelIds){
		this.forms = forms;
		this.formIds = formIds;
		this.lemmas = lemmas;
		this.lemmaIds = lemmaIds;
		this.cpostags = cpostags;
		this.cposIds = cposIds;
		this.postags = postags;
		this.posIds = posIds;
		this.morphs = morphs;
		this.morphIds = morphIds;
		this.heads = heads;
		this.deprels = deprels;
		this.deprelIds = deprelIds;
	}
	
	public void setTreeString(String treeStr){
		this.actParseTree = treeStr;
	}
	
	public String getTreeString(){
		return actParseTree;
	}
	
	public FeatureVector getFeatureVector(){
		return fv;
	}
	
	public void setFeatureVector(FeatureVector fv){
		this.fv = fv;
	}
	
	public int length(){
		return forms.length;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(Arrays.toString(forms)).append("\n");
		return sb.toString();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		//forms
		out.writeObject(forms);
		out.writeObject(formIds);
		//lemmas
		out.writeObject(lemmas);
		out.writeObject(lemmaIds);
		//cpos
		out.writeObject(cpostags);
		out.writeObject(cposIds);
		//pos
		out.writeObject(postags);
		out.writeObject(posIds);
		//feats
		out.writeObject(morphs);
		out.writeObject(morphIds);
		//heads
		out.writeObject(heads);
		//deprels
		out.writeObject(deprels);
		out.writeObject(deprelIds);
		//tree
		out.writeObject(actParseTree);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		//forms
		forms = (String[]) in.readObject();
		formIds = (int[]) in.readObject();
		//lemmas
		lemmas = (String[]) in.readObject();
		lemmaIds = (int[]) in.readObject();
		//cpos
		cpostags = (String[]) in.readObject();
		cposIds = (int[]) in.readObject();
		//pos
		postags = (String[]) in.readObject();
		posIds = (int[]) in.readObject();
		//feats
		morphs = (String[][]) in.readObject();
		morphIds = (int[][]) in.readObject();
		//heads
		heads = (int[]) in.readObject();
		//deprels
		deprels = (String[]) in.readObject();
		deprelIds = (int[]) in.readObject();
		//tree
		actParseTree = (String) in.readObject();
	}
}
