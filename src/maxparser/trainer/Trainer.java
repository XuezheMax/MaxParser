package maxparser.trainer;

import java.io.IOException;
import java.io.ObjectOutputStream;

import maxparser.exception.TrainingException;
import maxparser.io.ObjectIO;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public abstract class Trainer {
	public Trainer(){}
	
	public abstract void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException;
	
	protected void saveModel(ParserModel model, String file) throws IOException{
		ObjectOutputStream out = ObjectIO.getObjectOutputStream(file);
		out.writeObject(model);
		ObjectIO.close(out);
	}
}
