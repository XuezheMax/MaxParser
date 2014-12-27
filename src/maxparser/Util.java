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
}
