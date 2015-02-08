package maxparser;

public class Util {
	 // Assumes input is a String[] containing integers as strings.
	public static int[] stringsToInts(String[] stringreps) {
		int[] nums = new int[stringreps.length];
		for (int i = 0; i < stringreps.length; i++){
			nums[i] = Integer.parseInt(stringreps[i]);
		}
		return nums;
	}

	// Assumes input is a String[] containing doubles as strings.
	public static double[] stringsToDoubles(String[] stringreps) {
		double[] nums = new double[stringreps.length];
		for (int i = 0; i < stringreps.length; i++){
			nums[i] = Double.parseDouble(stringreps[i]);
		}
		return nums;
	}
	
	
	// log(exp(x) + exp(y));
	//  this can be used recursivly
	//e.g., log(exp(log(exp(x) + exp(y))) + exp(z)) =
	//log(exp (x) + exp(y) + exp(z))
	
	private static final int MINUS_LOG_EPSILON = 50;
	
	public static double logsumexp(double x, double y){
		double vmax = x > y ? x : y;
		double vmin = x > y ? y : x;
		
		if (vmax > vmin + MINUS_LOG_EPSILON) {
			return vmax;
		}
		else {
			return vmax + Math.log(1.0 + Math.exp(vmin - vmax));
		}
	}
}
