package edu.ucsd.grammar;

public interface ForClauseType<T extends ForClauseType<T>> {
	T getType();
	String getVariableAsString();
	String getFunctionName();
	String getParameterAsString();
	VariableTypes getVariableTypes(String variableName);
}
