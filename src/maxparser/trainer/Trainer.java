package maxparser.trainer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import maxparser.exception.TrainingException;
import maxparser.io.ObjectIO;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public abstract class Trainer {
	public Trainer(){}
	
	public abstract void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException;
	
	protected ParserModel loadModel(String file) throws TrainingException{
		try {
			ObjectInputStream in = ObjectIO.getObjectInputStream(file);
			ParserModel model = (ParserModel) in.readObject();
			ObjectIO.close(in);
			return model;
		} catch (IOException | ClassNotFoundException e) {
			throw new TrainingException(e.getMessage());
		}
		
	}
	
	protected void saveModel(ParserModel model, String file) throws TrainingException{
		try {
			ObjectOutputStream out = ObjectIO.getObjectOutputStream(file);
			out.writeObject(model);
			ObjectIO.close(out);
		} catch (IOException e) {
			throw new TrainingException(e.getMessage());
		}
	}
}
