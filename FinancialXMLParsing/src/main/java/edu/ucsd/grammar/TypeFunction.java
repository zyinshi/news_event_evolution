package edu.ucsd.grammar;

public interface TypeFunction {
	boolean isValid(VariableTypes callee, VariableTypes parameter);
}
