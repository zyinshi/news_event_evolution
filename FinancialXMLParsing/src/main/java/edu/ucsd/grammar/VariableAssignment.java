package edu.ucsd.grammar;

public class VariableAssignment implements ForClauseType<VariableAssignment> {
	private String variableName;
	private String functionName;
	private String argument;
	
	public VariableAssignment(String variableName, String functionName, String argument) {
		this.variableName = variableName;
		this.functionName = functionName;
		this.argument = argument;
	}

	@Override
	public VariableAssignment getType() {
		return this;
	}

	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	public String getArgument() {
		return this.argument;
	}
	
	@Override
	public VariableTypes getVariableTypes(String variableName) {
		return null;
	}

	@Override
	public String getVariableAsString() {
		return this.variableName;
	}

	@Override
	public String getParameterAsString() {
		return this.argument;
	}	
	
	public VariableTypes getInferredType() {
		// For now all functions will return phrases
		return VariableTypes.PHRASE;
	}
}
