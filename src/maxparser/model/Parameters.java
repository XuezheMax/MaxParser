package maxparser.model;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import maxparser.FeatureVector;
import maxparser.trainer.lbfgs.LBFGS.ExceptionWithIflag;
import maxparser.trainer.lbfgs.SimpleLBFGS;

public class Parameters implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double[] parameters = null;
	
	private double[] total = null;
	
	public Parameters(){}
	
	public Parameters(int size){
		parameters = new double[size];
		total = new double[size];
	}
	
	public Parameters(double[] parameters){
		this.parameters = parameters;
		this.total = null;
	}
	
	public void averageParams(double avVal){
		for(int i = 0; i < total.length; ++i){
			total[i] = total[i] / avVal;
		}
		parameters = total;
	}
	
	public Parameters getTemporalParames(double avVal){
		double[] tempParams = new double[total.length];
		for(int i = 0; i < total.length; ++i){
			tempParams[i] = total[i] / avVal;
		}
		return new Parameters(tempParams);
	}
	
	public void updateTotal(){
		for(int i = 0; i < total.length; ++i){
			total[i] += parameters[i];
		}
	}
	
	public double getScore(FeatureVector fv){
		return fv.getScore(parameters);
	}
	
	public void update(FeatureVector fv, double alpha_k, double upd){
		fv.update(parameters,  total, alpha_k, upd);
	}
	
	public void update(FeatureVector fv, double alpha_k){
		fv.update(parameters, alpha_k);
	}
	
	public int update(SimpleLBFGS lbfgs, double f, double[] g) throws ExceptionWithIflag{
		return lbfgs.optimize(parameters, f, g);
	}
	
	public double paramAt(int index){
		return parameters[index];
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(this.parameters);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		parameters = (double[]) in.readObject();
		total = null;
	}
}
