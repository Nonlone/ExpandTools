package per.nonlone.expandtweak.filter.exception;

public class FilterResultNullException extends Exception{

	private static final long serialVersionUID = 8606011358799454995L;
	
	public FilterResultNullException(String entityStr) {
		super(String.format("filterResult is null entityStr",entityStr));
	}

}
