package edu.ucsd.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WhereClause<T extends WhereClauseType<T>> {
	private Set<T> clauseTypes = new HashSet<T>();
	
	public WhereClause() {
	}
	
	public void addClauseType(T clauseType) {
		clauseTypes.add(clauseType);
	}
	
	public Set<T> getClauses() {
		return this.clauseTypes;
	}

	public Map<String, String> getParameters() {
		return clauseTypes.stream().filter(t -> t.getFunctionParameter() == null).collect(Collectors.toMap(t->t.getVariableName(), t->t.getVariableValue()));
	}
}
