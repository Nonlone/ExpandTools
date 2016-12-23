package per.nonlone.expandtweak.filter.exception;

public class UnKnownTypeException extends Exception{

	private static final long serialVersionUID = -6497495335300128225L;
	
	public UnKnownTypeException(String typeStr){
		super("unkown Type typeStr<"+typeStr+">");
	}
	
}
