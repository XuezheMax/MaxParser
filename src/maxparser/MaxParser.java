package maxparser;

import java.io.IOException;
import java.io.PrintWriter;

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
	 * @throws IOException 
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		ParserOptions options = new ParserOptions(args);
		if(options.getMode().equals("train")){
			ParserModel model = new ParserModel(options);
			Parser parser = (Parser) Class.forName(model.getParser()).newInstance();
			Trainer trainer = (Trainer) Class.forName(model.getTrainer()).newInstance();
			TypeLabeler typeLabeler = (TypeLabeler) Class.forName(model.getTypeLabeler()).newInstance();
			try {
				parser.train(trainer, typeLabeler, model, options.getTrainingFile(), options.getDevFile(), options.getLogFile(), options.getModelFile());
			} catch (TrainingException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		else if(options.getMode().equals("test")){
			System.out.print("Loading Model...");
			long clock = System.currentTimeMillis() / 1000;
			ParserModel model = Parser.loadModel(options.getModelFile());
			System.out.println("Done. Took: " + (System.currentTimeMillis() / 1000 - clock) + "s.");
			
			clock = System.currentTimeMillis() / 1000;
			Parser parser = (Parser) Class.forName(model.getParser()).newInstance();
			TypeLabeler typeLabeler = (TypeLabeler) Class.forName(model.getTypeLabeler()).newInstance();
			model.putReader(options.getReader());
			model.putWriter(options.getWriter());
			model.setPunctSet(options.getPunctSet());
			
			parser.parse(typeLabeler, model, options.getTestFile(), options.getOutFile());
			System.out.println("Done. Took: " + (System.currentTimeMillis() / 1000 - clock) + "s.");
			
			String goldfile = options.getGoldFile();
			if(goldfile != null){
				PrintWriter printer = new PrintWriter(System.out);
				printer.println("Performance:");
				maxparser.Evaluator.evaluate(options.getGoldFile(), options.getOutFile(), printer, model);
				printer.flush();
			}
		}
		else{
			System.err.println("unexpected mode: " + options.getMode());
		}
	}

}
