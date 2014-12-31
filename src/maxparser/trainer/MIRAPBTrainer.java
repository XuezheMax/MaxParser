package maxparser.trainer;

import java.io.IOException;
import java.io.ObjectInputStream;
import maxparser.io.ObjectIO;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.exception.TrainingException;
import maxparser.io.DependencyWriter;
import maxparser.model.ParserModel;
import maxparser.parser.decoder.Decoder;
import maxparser.parser.manager.Manager;

public class MIRAPBTrainer extends Trainer{
	
	@Override
	public void train(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException{
		if(devfile != null){
			trainWithDev(manager, decoder, model, trainfile, devfile, logfile, modelfile, numTrainInst, numDevInst);
		}
		else{
			trainWithoutDev(manager, decoder, model, trainfile, modelfile, numTrainInst);
		}
	}
	
	protected void trainWithDev(Manager manager, Decoder decoder, ParserModel model, String trainfile, String devfile, String logfile, String modelfile, int numTrainInst, int numDevInst) throws TrainingException{
		int numIters = model.iterNum();
		
		for(int i = 0; i < numIters; ++i){
			System.out.print("Iteration " + i + "[");
			long clock = System.currentTimeMillis() / 1000;
			//training iteration
			trainWithDevIter(manager, decoder, model, numTrainInst);
			System.out.println("\b\b\b" + numTrainInst);
			System.out.flush();
			
			//calc current accuracy
			ParserModel tempModel = model.getTemporalModel((i + 1) * numTrainInst);
			evalCurrentAcc(manager, decoder, tempModel, devfile, logfile, modelfile, numDevInst);
			System.out.println("|Time:" + (System.currentTimeMillis() / 1000 - clock) + "]");
			System.out.flush();
		}
	}
	
	protected void trainWithDevIter(Manager manager, Decoder decoder, ParserModel model, int numTrainInst) throws TrainingException{
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
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, model.trainingK());
			this.updateParams(inst, d, model);
		}
		
		//close in
		ObjectIO.close(in);
	}
	
	protected void evalCurrentAcc(Manager manager, Decoder decoder, ParserModel tempModel, String devfile, String logfile, String modelfile, int numDevInst) throws TrainingException{
		DependencyWriter tempWriter = null;
		ObjectInputStream in = ObjectIO.getObjectInputStream(tempModel.devforest());
		String tempfile = "tmp/result.tmp";
		
		try {
			tempWriter = DependencyWriter.createDependencyWriter(tempModel.getWriter());
			tempWriter.startWriting(tempfile);
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new TrainingException(e.getMessage());
		}
		
		for(int j = 0; j < numDevInst; ++j){
			DependencyInstance inst = manager.readInstance(in, tempModel);
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, 1);
			String[] res = d[0].second.split(" ");
			int[] heads = new int[inst.length()];
			String[] types = new String[inst.length()];
			heads[0] = -1;
			types[0] = "<no-type>";
			for(int k = 1; k < inst.length(); k++){
				String[] trip = res[k - 1].split("\\|:");
				heads[k] = Integer.parseInt(trip[0]);
				types[k] = tempModel.getType(Integer.parseInt(trip[2]));
			}
			
			try {
				tempWriter.write(inst, heads, types);
			} catch (IOException e) {
				throw new TrainingException(e.getMessage());
			}
		}
		
		//evaluate current acc
		// TODO
		
		//close in & tempWriter
		ObjectIO.close(in);
		try {
			tempWriter.close();
		} catch (IOException e) {
			throw new TrainingException(e.getMessage());
		}
	}
	
	protected void trainWithoutDev(Manager manager, Decoder decoder, ParserModel model, String trainfile, String modelfile, int numTrainInst) throws TrainingException{
		int numIters = model.iterNum();
		for(int i = 0; i < numIters; ++i){
			System.out.print("Iteration " + i + "[");
			long clock = System.currentTimeMillis() / 1000;
			trainWithoutDevIter(manager, decoder, model, numTrainInst, numIters, i);
			System.out.println(numTrainInst + "|Time:" + (System.currentTimeMillis() / 1000 - clock) + "]");
			System.out.flush();
		}
		model.averageParams(numIters * numTrainInst);
		System.out.print("Saving Model...");
		long clock = System.currentTimeMillis() / 1000;
		saveModel(model, modelfile);
		System.out.println("Done. Took: " + (System.currentTimeMillis() / 1000 - clock) + "s.");
	}
	
	protected void trainWithoutDevIter(Manager manager, Decoder decoder, ParserModel model, int numTrainInst, int numIters, int iter) throws TrainingException{
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
			Pair<FeatureVector, String>[] d = decoder.decode(manager, inst, model.trainingK());
			this.updateParams(inst, d, upd, model);
		}
		System.out.print("\b\b\b");
		
		//close in
		ObjectIO.close(in);
	}
	
	public void updateParams(DependencyInstance inst, Pair<FeatureVector, String>[] d, double upd, ParserModel model){
		String actParseTree = inst.getTreeString();
		FeatureVector actFV = inst.getFeatureVector();

		int K = 0;
		for (int i = 0; i < d.length && d[i].first != null; i++) {
			K = i + 1;
		}

		double[] b = new double[K];
		double[] lam_dist = new double[K];
		FeatureVector[] dist = new FeatureVector[K];

		for (int k = 0; k < K; k++) {
			lam_dist[k] = model.getScore(actFV) - model.getScore(d[k].first);
			b[k] = (double) numErrors(inst, d[k].second, actParseTree, model);
			b[k] -= lam_dist[k];
			dist[k] = actFV.getDistVector(d[k].first);
		}

		double[] alpha = hildreth(dist, b);

		for (int k = 0; k < K; k++) {
			model.update(dist[k], alpha[k], upd);
		}
	}
	
	public void updateParams(DependencyInstance inst, Pair<FeatureVector, String>[] d, ParserModel model){
		String actParseTree = inst.getTreeString();
		FeatureVector actFV = inst.getFeatureVector();

		int K = 0;
		for (int i = 0; i < d.length && d[i].first != null; i++) {
			K = i + 1;
		}

		double[] b = new double[K];
		double[] lam_dist = new double[K];
		FeatureVector[] dist = new FeatureVector[K];

		for (int k = 0; k < K; k++) {
			lam_dist[k] = model.getScore(actFV) - model.getScore(d[k].first);
			b[k] = (double) numErrors(inst, d[k].second, actParseTree, model);
			b[k] -= lam_dist[k];
			dist[k] = actFV.getDistVector(d[k].first);
		}

		double[] alpha = hildreth(dist, b);
		
		for (int k = 0; k < K; k++) {
			model.update(dist[k], alpha[k]);
		}
		model.updateTotal();
	}
	
	private double[] hildreth(FeatureVector[] a, double[] b) {

		int i;
		int max_iter = 10000;
		double eps = 0.00000001;
		double zero = 0.000000000001;

		double[] alpha = new double[b.length];

		double[] F = new double[b.length];
		double[] kkt = new double[b.length];
		double max_kkt = Double.NEGATIVE_INFINITY;

		int K = a.length;

		double[][] A = new double[K][K];
		boolean[] is_computed = new boolean[K];
		for (i = 0; i < K; i++) {
			A[i][i] = a[i].dotProduct(a[i]);
			is_computed[i] = false;
		}

		int max_kkt_i = -1;

		for (i = 0; i < F.length; i++) {
			F[i] = b[i];
			kkt[i] = F[i];
			if (kkt[i] > max_kkt) {
				max_kkt = kkt[i];
				max_kkt_i = i;
			}
		}

		int iter = 0;
		double diff_alpha;
		double try_alpha;
		double add_alpha;

		while (max_kkt >= eps && iter < max_iter) {

			diff_alpha = A[max_kkt_i][max_kkt_i] <= zero ? 0.0 : F[max_kkt_i] / A[max_kkt_i][max_kkt_i];
			try_alpha = alpha[max_kkt_i] + diff_alpha;
			add_alpha = 0.0;

			if (try_alpha < 0.0)
				add_alpha = -1.0 * alpha[max_kkt_i];
			else
				add_alpha = diff_alpha;

			alpha[max_kkt_i] = alpha[max_kkt_i] + add_alpha;

			if (!is_computed[max_kkt_i]) {
				for (i = 0; i < K; i++) {
					A[i][max_kkt_i] = a[i].dotProduct(a[max_kkt_i]); // for version 1
					is_computed[max_kkt_i] = true;
				}
			}

			for (i = 0; i < F.length; i++) {
				F[i] -= add_alpha * A[i][max_kkt_i];
				kkt[i] = F[i];
				if (alpha[i] > zero)
					kkt[i] = Math.abs(F[i]);
			}

			max_kkt = Double.NEGATIVE_INFINITY;
			max_kkt_i = -1;
			for (i = 0; i < F.length; i++)
				if (kkt[i] > max_kkt) {
					max_kkt = kkt[i];
					max_kkt_i = i;
				}

			iter++;
		}
		return alpha;
	}
	
	public double numErrors(DependencyInstance inst, String pred, String act, ParserModel model) {
		if (model.lossType().equals("nopunc")){
			return numErrorsDepNoPunc(inst, pred, act, model) + numErrorsLabelNoPunc(inst, pred, act, model);
		}
		else{
			return numErrorsDep(inst, pred, act) + numErrorsLabel(inst, pred, act);
		}
	}

	public double numErrorsDep(DependencyInstance inst, String pred, String act) {

		String[] act_spans = act.split(" ");
		String[] pred_spans = pred.split(" ");

		int correct = 0;

		for (int i = 0; i < pred_spans.length; i++) {
			int position = pred_spans[i].indexOf(':');
			String p = pred_spans[i].substring(0, position);
			position = act_spans[i].indexOf(':');
			String a = act_spans[i].substring(0, position);
			if (p.equals(a)) {
				correct++;
			}
		}

		return ((double) act_spans.length - correct);
	}

	public double numErrorsLabel(DependencyInstance inst, String pred, String act) {

		String[] act_spans = act.split(" ");
		String[] pred_spans = pred.split(" ");

		int correct = 0;

		for (int i = 0; i < pred_spans.length; i++) {
			int position = pred_spans[i].indexOf(':');
			String p = pred_spans[i].substring(position + 1);
			position = act_spans[i].indexOf(':');
			String a = act_spans[i].substring(position + 1);
			if (p.equals(a)) {
				correct++;
			}
		}
		return ((double) act_spans.length - correct);
	}

	public double numErrorsDepNoPunc(DependencyInstance inst, String pred, String act, ParserModel model) {

		String[] act_spans = act.split(" ");
		String[] pred_spans = pred.split(" ");

		String[] pos = inst.postags;

		int correct = 0;
		int numPunc = 0;

		for (int i = 0; i < pred_spans.length; i++) {
			int position = pred_spans[i].indexOf(':');
			String p = pred_spans[i].substring(0, position);
			position = act_spans[i].indexOf(':');
			String a = act_spans[i].substring(0, position);
			if(model.isPunct(pos[i + 1])){
				numPunc++;
				continue;
			}
			if (p.equals(a)) {
				correct++;
			}
		}

		return ((double) act_spans.length - numPunc - correct);
	}

	public double numErrorsLabelNoPunc(DependencyInstance inst, String pred, String act, ParserModel model) {

		String[] act_spans = act.split(" ");
		String[] pred_spans = pred.split(" ");

		String[] pos = inst.postags;

		int correct = 0;
		int numPunc = 0;

		for (int i = 0; i < pred_spans.length; i++) {
			int position = pred_spans[i].indexOf(':');
			String p = pred_spans[i].substring(position + 1);
			position = act_spans[i].indexOf(':');
			String a = act_spans[i].substring(position + 1);
			if(model.isPunct(pos[i + 1])){
				numPunc++;
				continue;
			}
			if (p.equals(a)) {
				correct++;
			}
		}

		return ((double) act_spans.length - numPunc - correct);
	}
}
