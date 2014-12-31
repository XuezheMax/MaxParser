package maxparser.exception;

public class TrainingException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TrainingException(){}
	
	public TrainingException(String message){
		super(message);
	}
	
	public TrainingException(Throwable cause){
		super(cause);
	}

}
