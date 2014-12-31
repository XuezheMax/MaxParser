package maxparser;

import maxparser.parser.Parser;
import maxparser.parser.typelabler.TypeLabeler;
import maxparser.exception.TrainingException;
import maxparser.model.ParserModel;
import maxparser.model.ParserOptions;
import maxparser.trainer.Trainer;

public class MaxParser {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		ParserOptions options = new ParserOptions(args);
		if(options.getMode().equals("train")){
			Parser parser = (Parser) Class.forName(options.getParser()).newInstance();
			Trainer trainer = (Trainer) Class.forName(options.getTrainer()).newInstance();
			TypeLabeler typeLabeler = (TypeLabeler) Class.forName(options.getTypeLabeler()).newInstance();
			ParserModel model = new ParserModel(options);
			try {
				parser.train(trainer, typeLabeler, model, options.getTrainingFile(), options.getDevFile(), options.getLogFile());
			} catch (TrainingException e) {
				e.printStackTrace();
			}
		}
	}

}
