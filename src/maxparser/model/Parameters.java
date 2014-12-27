package maxparser.model;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import maxparser.FeatureVector;

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
		for(int i = 0; i < size; ++i){
			parameters[i] = 0.0;
			total[i] = 0.0;
		}
	}
	
	public Parameters(double[] parameters){
		this.parameters = parameters;
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
	
	public double getScore(FeatureVector fv){
		return fv.getScore(parameters);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.writeObject(this.parameters);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		parameters = (double[]) in.readObject();
	}
}
