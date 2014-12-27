package maxparser.trainer;

import maxparser.DependencyInstance;
import maxparser.FeatureVector;
import maxparser.Pair;
import maxparser.model.ParserModel;

public class MIRAPBTrainer extends Trainer{
	
	public void updateParams(DependencyInstance inst, Pair<FeatureVector, String> d, double upd, ParserModel model){
		
	}
	
	public void updateParams(DependencyInstance inst, Pair<FeatureVector, String> d, ParserModel model){
		
	}
	
	public double numErrors(DependencyInstance inst, String pred, String act, String lossType, ParserModel model) {
		if (lossType.equals("nopunc")){
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
