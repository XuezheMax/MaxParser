package maxparser.trainer.lbfgs;

import maxparser.trainer.lbfgs.LBFGS.ExceptionWithIflag;

public class SimpleLBFGS {
	private final int m = 5;
	private final double eps = 1e-7;
	private final double xtol = 1e-16;
	
	private double[] diag = null;
	private boolean diagco = false;
	
	private int[] iflag = null;
	
	private int[] iprint = null;
	
	int n = 0;
	
	public SimpleLBFGS(int n){
		this.n = n;
		
		iflag = new int[1];
		iflag[0] = 0;
		
		iprint = new int[2];
		iprint[0] = -1;
		iprint[1] = 0;
		
		diag = new double[n];
	}
	
	public int optimize(double[] x, double f, double[] g) throws ExceptionWithIflag{
		LBFGS.lbfgs(n, m, x, f, g, diagco, diag, iprint, eps, xtol, iflag);
		
		if(iflag[0] < 0){
			return -1; // error
		}
		else if(iflag[0] == 0){
			return 0; // terminate
		}
		else{
			return 1; // next f and g
		}
	}
}
