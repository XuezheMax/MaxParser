package maxparser.parser.manager;

import java.io.IOException;
import java.util.ArrayList;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.DependencyReader;
import maxparser.io.ObjectReader;
import maxparser.io.ObjectWriter;
import maxparser.model.ParserModel;
import maxparser.parser.indextuple.IndexTuple;
import maxparser.parser.manager.featgen.FeatureGenerator;
import maxparser.parser.typelabeler.TypeLabeler;

public abstract class Manager {
	protected FeatureGenerator featGen = null;
	protected TypeLabeler typelabeler = null;
	
	public String genTreeString(int[] heads, int[] types){
		StringBuffer spans = new StringBuffer(heads.length * 5);
		for(int i = 1; i < heads.length; i++){
			spans.append(heads[i]).append("|").append(i).append(":")
                .append(types[i]).append(" ");
		}
		return spans.substring(0, spans.length() - 1);
	}
	
	public Pair<int[], int[]> getHeadsTypesfromTreeString(String treeStr){
		String[] tokens = treeStr.split(" ");
		int length = tokens.length + 1;
		Pair<int[], int[]> result = new Pair<int[], int[]>(new int[length], new int[length]);
		result.first[0] = -1;
		result.second[0] = 0;
		for(int k = 1; k < length; ++k){
			String[] trip = tokens[k - 1].split("[\\|:]");
			result.first[k] = Integer.parseInt(trip[0]);
			result.second[k] = Integer.parseInt(trip[2]);
		}
		return result;
	}
	
	public void setTypeLabeler(TypeLabeler typeLabeler){
		this.typelabeler = typeLabeler;
	}
	
	private ArrayList<DependencyInstance> createAlphabets(String trainfile, ParserModel model) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		ArrayList<DependencyInstance> instList = new ArrayList<DependencyInstance>();
		DependencyReader reader = DependencyReader.createDependencyReader(model.getReader());
		reader.startReading(trainfile);
		System.out.println("Creating Alphabet ... ");
		DependencyInstance instance = reader.getNext(model);
		while(instance != null){
			instance.setFeatureVector(createFeatureVector(instance, model));
			instance.setTreeString(genTreeString(instance.heads, instance.deprelIds));
			instList.add(instance);
			instance = reader.getNext(model);
		}
		model.closeAlphabets();
		System.out.println("Done.");
		reader.close();
		return instList;
	}
	
	public final int[] createInstance(String trainfile, String trainforest, String devfile, String devforest, ParserModel model) throws TrainingException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		long clock = System.currentTimeMillis();
		int[] results = new int[3];
		ArrayList<DependencyInstance> instList = createAlphabets(trainfile, model);
		
		System.out.println("Num Features: " + model.featureSize());
		System.out.println("Num Forms: " + model.formSize());
		System.out.println("Num Lemmas: " + model.lemmaSize());
		System.out.println("Num CPOS tags: " + model.cposSize());
		System.out.println("Num POS tags: " + model.posSize());
		System.out.println("Num Type tags: " + model.typeSize());
		
		int maxLength = 0;
		for(DependencyInstance inst : instList){
			if(inst.length() > maxLength){
				maxLength = inst.length();
			}
		}
		
		boolean createforest = model.createforest();
		if(createforest){
			System.out.println("Creating Training Instances: ");
			int threadNum = model.threadNum();
			int numInst = instList.size();
			int unit = (numInst % threadNum == 0) ? numInst / threadNum : numInst / threadNum + 1;
			CreateForestThread[] threads = new CreateForestThread[threadNum];
			for(int i = 0; i < threadNum; i++){
				int start = unit * i;
				int end = start + unit;
				if(end > numInst){
					end = numInst;
				}
				String[] tokens = trainforest.split("\\.");
				String forestfile = tokens[0] + i + "." + tokens[1];
				threads[i] = new CreateForestThread(start, end, instList, this, forestfile, model);
			}
			for(CreateForestThread thread : threads){
				thread.start();
			}
			for(CreateForestThread thread : threads){
				try {
					thread.join();
				} catch (InterruptedException e) {
					throw new TrainingException(e.getMessage());
				}
			}
			System.out.println("Done.");
		}
		results[1] = instList.size();
		results[2] = 0;
		
		if(devfile != null){
			ObjectWriter out = createforest ? new ObjectWriter(devforest) : null;
			DependencyReader reader = DependencyReader.createDependencyReader(model.getReader());
			reader.startReading(devfile);
			System.out.println("Creating development Instances: ");
			int i = 0;
			DependencyInstance instance = reader.getNext(model);
			while(instance != null){
				System.out.print(i++ + " ");
				System.out.flush();
				results[2]++;
				instance.setFeatureVector(createFeatureVector(instance, model));
				instance.setTreeString(genTreeString(instance.heads, instance.deprelIds));
				if(createforest){
					this.writeInstance(instance, out, model);
				}
				if(instance.length() > maxLength){
					maxLength = instance.length();
				}
				instance = reader.getNext(model);
			}
			reader.close();
			if(createforest){
				out.close();
			}
			System.out.println("Done.");
		}
		results[0] = maxLength;
		System.out.println("Took " + (System.currentTimeMillis() - clock) / 1000 + "s.");
		return results;
	}
	
	public abstract void adjustEdgeLoss(DependencyInstance inst, ParserModel model);
	
	public final FeatureVector createFeatureVector(DependencyInstance inst, ParserModel model){
		FeatureVector fv = new FeatureVector();
		featGen.genUnlabeledFeatures(inst, model, fv);
		typelabeler.genLabeledFeatures(inst, model, fv);
		return fv;
	}
	
	protected final void writeInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException{
		writeUnlabeledInstance(inst, out, model);
		typelabeler.writeLabeledInstance(inst, out, model);
	}
	
	public final DependencyInstance readInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException{
		DependencyInstance inst = readUnlabeledInstance(in, model);
		typelabeler.readLabeledInstance(in, model);
		return inst;
	}
	
	public final void fillFeatureVector(DependencyInstance inst, ParserModel model){
		fillUnlabeledFeatureVector(inst, model);
		typelabeler.fillLabeledFeatureVector(inst, model);
	}
	
	public final double getScore(IndexTuple itemId){
		return getUnlabeledScore(itemId) + typelabeler.getLabeledScore(itemId);
	}
	
	public void getType(DependencyInstance inst, IndexTuple itemId, ParserModel model){
		typelabeler.getType(inst, itemId, model);
	}
	
	public void getTypes(int length, ParserModel model){
		typelabeler.getTypes(length, model);
	}
	
	protected abstract void writeUnlabeledInstance(DependencyInstance inst, ObjectWriter out, ParserModel model) throws IOException;
	
	protected abstract DependencyInstance readUnlabeledInstance(ObjectReader in, ParserModel model) throws IOException, ClassNotFoundException;
	
	protected abstract void fillUnlabeledFeatureVector(DependencyInstance inst, ParserModel model);
	
	public abstract void init(int size);
	
	public abstract int size();
	
	public abstract Manager clone(int size, int type_size);
	
	protected abstract double getUnlabeledScore(IndexTuple itemId);
}

class CreateForestThread extends Thread{
	private int start;
	private int end;
	private ArrayList<DependencyInstance> instList;
	private Manager manager;
	private String forestfile;
	private ParserModel model;
	public CreateForestThread(int start, int end, ArrayList<DependencyInstance> instList, Manager manager, String forestfile, ParserModel model){
		this.start = start;
		this.end = end;
		this.instList = instList;
		this.manager = manager;
		this.forestfile = forestfile;
		this.model = model;
	}
	
	public void run(){
		try {
			ObjectWriter out = new ObjectWriter(forestfile);
			for(int i = start; i < end; ++i){
				System.out.print(i + " ");
				System.out.flush();
				manager.writeInstance(instList.get(i), out, model);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}