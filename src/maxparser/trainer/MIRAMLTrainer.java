package maxparser.trainer;

import java.io.IOException;
import java.io.ObjectInputStream;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.io.ObjectIO;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public class MIRAMLTrainer extends MIRAPBTrainer{
	
	@Override
	protected void trainWithDevIter(Manager manager, Decoder decoder, ParserModel model, int numTrainInst) throws ClassNotFoundException, IOException{
		ObjectInputStream in = null;
		int threadNum = model.threadNum();
		String[] tokens = model.trainforest().split("\\.");
		int unit = (numTrainInst % threadNum == 0) ? numTrainInst / threadNum : numTrainInst / threadNum + 1;
		for(int j = 0; j < numTrainInst; ++j){
			if(j % 500 == 0){
				int percent = j * 100 / numTrainInst;
				System.out.print("\b\b\b" + (percent < 10 ? " " : "") + percent + "%");
				System.out.flush();
			}
			if(j % unit == 0){
				ObjectIO.close(in);
				String forestfile = tokens[0] + (j / unit) + "." + tokens[1];
				in = ObjectIO.getObjectInputStream(forestfile);
			}
			
			DependencyInstance inst = manager.readInstance(in, model);
			manager.adjustEdgeLoss(inst, model);
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, model.trainingK(), model);
			this.updateParams(inst, d, model);
		}
		System.out.print("\b\b\b");
		//close in
		ObjectIO.close(in);
	}
	
	@Override
	protected void trainWithoutDevIter(Manager manager, Decoder decoder, ParserModel model, int numTrainInst, int numIters, int iter) throws ClassNotFoundException, IOException{
		ObjectInputStream in = null;
		int threadNum = model.threadNum();
		String[] tokens = model.trainforest().split("\\.");
		int unit = (numTrainInst % threadNum == 0) ? numTrainInst / threadNum : numTrainInst / threadNum + 1;
		for(int j = 0; j < numTrainInst; ++j){
			if(j % 500 == 0){
				int percent = j * 100 / numTrainInst;
				System.out.print("\b\b\b" + (percent < 10 ? " " : "") + percent + "%");
				System.out.flush();
			}
			if(j % unit == 0){
				ObjectIO.close(in);
				String forestfile = tokens[0] + (j / unit) + "." + tokens[1];
				in = ObjectIO.getObjectInputStream(forestfile);
			}
			
			double upd = (numIters * numTrainInst - (numTrainInst * iter + (j + 1)) + 1);
			DependencyInstance inst = manager.readInstance(in, model);
			manager.adjustEdgeLoss(inst, model);
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, model.trainingK(), model);
			this.updateParams(inst, d, upd, model);
		}
		System.out.print("\b\b\b");
		
		//close in
		ObjectIO.close(in);
	}
}
