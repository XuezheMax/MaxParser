package maxparser.trainer;

import java.io.IOException;

import maxparser.exception.TrainingException;
import maxparser.io.ObjectWriter;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public abstract class Trainer {
	public Trainer(){}
	
	public abstract void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException;
	
	protected void saveModel(ParserModel model, String file) throws IOException{
		ObjectWriter out = new ObjectWriter(file);
		out.writeObject(model);
		out.close();
	}
}
