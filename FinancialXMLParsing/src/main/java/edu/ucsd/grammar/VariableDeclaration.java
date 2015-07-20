package edu.ucsd.grammar;

public class VariableDeclaration implements ForClauseType<VariableDeclaration> {
	private String variableName;
	private String variableType;
	private VariableTypes variableTypes;

	public VariableDeclaration(String variableName, String variableType) {
		this.variableName = variableName;
		this.variableType = variableType;
		this.variableTypes = VariableTypes.fromString(variableType);
	}

	@Override
	public VariableDeclaration getType() {
		return this;
	}
	
	@Override
	public VariableTypes getVariableTypes(String variableName) {
		if(this.variableName.equals(variableName)) {
			return this.variableTypes;
		}
		
		return null;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public String getVariableType() {
		return this.variableType;
	}
	
	@Override
	public String getFunctionName() {
		return null;
	}

	@Override
	public String getVariableAsString() {
		return this.variableName;
	}

	@Override
	public String getParameterAsString() {
		return null;
	}	
}
