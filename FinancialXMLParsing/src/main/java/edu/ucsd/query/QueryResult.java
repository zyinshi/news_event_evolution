package edu.ucsd.query;

public class QueryResult<T> {
	private Class<T> resultType;
	private Object result;
	
	public QueryResult(Object result, Class<T> resultType) {
		this.result = result;
		this.resultType = resultType;
	}
	
	public T getResult() {
		return this.resultType.cast(result);
	}
}
