package edu.ucsd.grammar;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ForClause<T extends ForClauseType<T>> {
	private Set<T> clauses = new HashSet<T>(); 
	
	public ForClause() {
	}
	
	public void addClauseType(T clauseType) {
		clauses.add(clauseType);
	}
	
	public Set<T> getClauses() {
		return this.clauses;
	}

	public Set<T> getAllFunctions() {
		return clauses.stream().filter(t -> t.getFunctionName() != null).collect(Collectors.toSet());
	}

	public VariableTypes getVariableTypes(String variableName) {
		for(T clause : clauses) {
			if(clause.getVariableAsString().equals(variableName)) {
				return clause.getVariableTypes(variableName);
			}
		}
		
		return null;
	}
}
