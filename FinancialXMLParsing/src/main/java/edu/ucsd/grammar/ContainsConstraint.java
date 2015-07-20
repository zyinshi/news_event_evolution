package edu.ucsd.grammar;

class ContainsConstraint implements WhereClauseType<ContainsConstraint> {
	private String variableName;
	private String function;
	private String functionParameter;

	public ContainsConstraint(String variableName, String function, String functionParameter) {
		this.variableName = variableName;
		this.function = function.substring(1);;
		this.functionParameter = functionParameter;
	}
	
	@Override
	public String getVariableName() {
		return this.variableName;
	}

	@Override
	public String getFunctionName() {
		return this.function;
	}

	@Override
	public String getFunctionParameter() {
		return this.functionParameter;
	}
	
	@Override
	public boolean usesVariableName(String variableName) {
		return variableName.equals(this.variableName) || variableName.equals(getFunctionParameter());
	}
}
