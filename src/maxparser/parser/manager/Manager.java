package maxparser.parser.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import maxparser.DependencyInstance;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.DependencyReader;
import maxparser.io.ObjectIO;
import maxparser.model.ParserModel;
import maxparser.parser.featgen.FeatureGenerator;
import maxparser.parser.typelabler.TypeLabeler;

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
			instance.setFeatureVector(featGen.createFeatureVector(instance, model));
			instance.setTreeString(genTreeString(instance.heads, instance.deprelIds));
			instList.add(instance);
			instance = reader.getNext(model);
		}
		model.closeAlphabets();
		System.out.println("Done.");
		reader.close();
		return instList;
	}
	
	public int[] createInstance(String trainfile, String trainforest, String devfile, String devforest, ParserModel model) throws TrainingException{
		long clock = System.currentTimeMillis();
		int[] results = new int[3];
		ArrayList<DependencyInstance> instList = null;
		try {
			instList = createAlphabets(trainfile, model);
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | IOException e) {
			throw new TrainingException(e.getMessage());
		}
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
		}
		results[1] = instList.size();
		results[2] = 0;
		
		if(devfile != null){
			try {
				ObjectOutputStream out = createforest ? ObjectIO.getObjectOutputStream(devforest) : null;
				DependencyReader reader = DependencyReader.createDependencyReader(model.getReader());
				reader.startReading(devfile);
				DependencyInstance instance = reader.getNext(model);
				while(instance != null){
					results[2]++;
					instance.setFeatureVector(featGen.createFeatureVector(instance, model));
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
			} catch (IOException | ReflectiveOperationException e) {
				throw new TrainingException(e.getMessage());
			}
		}
		results[0] = maxLength;
		System.out.println("Took " + (System.currentTimeMillis() - clock) / 1000 + "s.");
		return results;
	}
	
	protected abstract void writeInstance(DependencyInstance inst, ObjectOutputStream out, ParserModel model);
	public abstract DependencyInstance readInstance(ObjectInputStream in, ParserModel model);
	public abstract void init(int maxLength);
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
			ObjectOutputStream out = ObjectIO.getObjectOutputStream(forestfile);
			for(int i = start; i < end; i++){
				manager.writeInstance(instList.get(i), out, model);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}