package edu.ucsd.grammar;

public interface WhereClauseType<T extends WhereClauseType<T>> {
	String getVariableName();
	default String getVariableValue() {
		return null;
	}
	String getFunctionParameter();
	default String getFunctionName() {
		return null;
	}
	boolean usesVariableName(String variableName);
}
