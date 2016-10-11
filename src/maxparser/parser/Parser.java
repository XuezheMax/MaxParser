package maxparser.parser;

import java.io.IOException;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.*;
import maxparser.io.DependencyReader;
import maxparser.io.DependencyWriter;
import maxparser.model.ParserModel;
import maxparser.parser.manager.Manager;
import maxparser.parser.typelabeler.TypeLabeler;
import maxparser.parser.decoder.Decoder;
import maxparser.trainer.Trainer;
import maxparser.io.ObjectReader;

public abstract class Parser {
	protected ParserModel model = null;
	protected Manager manager = null;
	protected Decoder decoder = null;
	
	public Parser(){}
	
	public void train(Trainer trainer, TypeLabeler typeLabeler, ParserModel model, String trainfile, String devfile, String logfile, String modelfile) throws TrainingException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		this.model = model;
		manager.setTypeLabeler(typeLabeler);
		int[] nums= manager.createInstance(trainfile, model.trainforest(), devfile, model.devforest(), model);
		model.createParameters();
		manager.init(nums[0]);
		typeLabeler.init(nums[0], model.typeSize());
		trainer.train(manager, decoder, model, trainfile, devfile, logfile, modelfile, nums[1], nums[2]);
	}
	
	public void parse(TypeLabeler typeLabeler, ParserModel model, String testfile, String outfile) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException{
		this.model = model;
		manager.setTypeLabeler(typeLabeler);
		DependencyReader reader = DependencyReader.createDependencyReader(model.getReader());
		DependencyWriter writer = DependencyWriter.createDependencyWriter(model.getWriter());
		reader.startReading(testfile);
		writer.startWriting(outfile);
		System.out.print("Parsing Sentence: ");
		DependencyInstance inst = reader.getNext(model);
		int sent_num = 0;
		int maxLength = 0;
		while(inst != null){
			System.out.print(++sent_num + " ");
			int length = inst.length();
			if(length > maxLength){
				maxLength = length;
				manager.init(maxLength);
			}
			manager.fillFeatureVector(inst, model);
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, 1, model);
			String[] res = d[0].second.split(" ");
			int[] heads = new int[inst.length()];
			String[] types = new String[inst.length()];
			heads[0] = -1;
			types[0] = "<no-type>";
			for(int k = 1; k < inst.length(); k++){
				String[] trip = res[k - 1].split("[\\|:]");
				heads[k] = Integer.parseInt(trip[0]);
				types[k] = model.getType(Integer.parseInt(trip[2]));
			}
			writer.write(inst, heads, types);
			inst = reader.getNext(model);
		}
		System.out.println();
		reader.close();
		writer.close();
	}
	
	public static ParserModel loadModel(String file) throws ClassNotFoundException, IOException{
		ObjectReader in = new ObjectReader(file);
		ParserModel model = (ParserModel) in.readObject();
		in.close();
		return model;
	}
}
