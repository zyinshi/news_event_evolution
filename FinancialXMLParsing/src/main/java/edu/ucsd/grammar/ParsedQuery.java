package edu.ucsd.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ParsedQuery<F extends ForClauseType<F>, W extends WhereClauseType<W>> {
	private ForClause<F> forClause;
	private WhereClause<W> whereClause;
	private ReturnClause returnClause;
	
	public ParsedQuery(ForClause<F> forClause, WhereClause<W> whereClause, ReturnClause returnClause) {
		this.forClause = forClause;
		this.whereClause = whereClause;
		this.returnClause = returnClause;
	}

	public ForClause<F> getForClause() {
		return forClause;
	}

	public WhereClause<W> getWhereClause() {
		return whereClause;
	}

	public ReturnClause getReturnClause() {
		return returnClause;
	}

	public void validate() throws ValidationException {
		Set<String> variables = validateForClause();
		validateWhereAndReturnClause(variables);
	}

	private void validateWhereAndReturnClause(Set<String> variables) {
		Set<W> clauses = this.getWhereClause().getClauses();
		List<String> variableNames = clauses.stream().filter(w -> w.getVariableName() != null).map(w -> w.getVariableName()).collect(Collectors.toList());
		Set<String> variableNamesAsSet = variableNames.stream().collect(Collectors.toCollection(HashSet::new));
		if(variableNamesAsSet.size() < variableNames.size()) {
			System.out.println(variableNamesAsSet);
			System.out.println(variableNames);
			throw new ValidationException("Duplicate Parameters in where clause.");
		}
		for(String variableName : variableNamesAsSet) {
			if(!variables.contains(variableName)) {
				throw new ValidationException("Undeclared variable in where clause.");
			}
		}
		Set<String> parameterNames = clauses.stream().filter(w -> w.getFunctionParameter() != null).map(w -> w.getFunctionParameter()).collect(Collectors.toCollection(HashSet::new));
		for(String parameterName : parameterNames) {
			if(!variables.contains(parameterName)) {
				throw new ValidationException("Undeclared variable used as parameter in where clause.");
			}
		}
		
		// Validate return clause
		String returnVariableName = this.getReturnClause().getVariableName();
		if(!variables.contains(returnVariableName)) {
			throw new ValidationException("Undeclared variable in return clause.");
		}
		
		// Validate if there are unused variables that don't contribute to the return statement
		Map<String, W> functionWhereClauses = clauses.stream().filter(w->w.getFunctionParameter() != null).collect(Collectors.toMap(w -> w.getVariableName(), w -> w));
		Set<String> filterCriteriaAssignments = clauses.stream().filter(w -> w.getFunctionParameter() == null).map(w-> w.getVariableName()).collect(Collectors.toCollection(HashSet::new));
		Set<String> forVarNoFunctions = this.forClause.getClauses().stream().filter(f -> f.getFunctionName() == null).map(f -> f.getVariableAsString()).collect(Collectors.toCollection(HashSet::new));
		boolean notUsed = true;
		for(String variable : forVarNoFunctions) {
			// Check Variable Assignments
			notUsed = notUsed && !filterCriteriaAssignments.contains(variable);
			// Check Function calls
			for(W function : functionWhereClauses.values()) {
				notUsed = notUsed && !function.usesVariableName(variable);
			}
			// Check Return Statement
			notUsed = notUsed && !returnVariableName.equals(variable);
			if(notUsed) {
				throw new ValidationException("Parameter is declared but does not contribute to the where and return statements.");
			}
			notUsed = true;
		}
		
		W whereClause = functionWhereClauses.remove(returnVariableName);
		if(whereClause != null) { // return variable is specified as part of the where clause
			filterCriteriaAssignments.remove(whereClause.getFunctionParameter());
			Map<String, F> forClauseAssignments = this.forClause.getClauses().stream().filter(f -> f.getFunctionName() != null).collect(Collectors.toMap(f -> f.getVariableAsString(), f -> f));
			F f = forClauseAssignments.remove(whereClause.getFunctionParameter());
			if(f != null) {
				filterCriteriaAssignments.remove(f.getParameterAsString());
			}
			if(functionWhereClauses.size() > 0 || filterCriteriaAssignments.size() > 0) {
				throw new ValidationException("Parameter is declared and set but does not contribute to the return statement.");
			}
		} else { // return variable is specified as part of the for clause
			Map<String, F> forClauseAssignments = this.forClause.getClauses().stream().filter(f -> f.getFunctionName() != null).collect(Collectors.toMap(f -> f.getVariableAsString(), f -> f));
			F assignment = forClauseAssignments.remove(returnVariableName);	
			if(assignment != null) {
				filterCriteriaAssignments.remove(assignment.getParameterAsString());
				if(forClauseAssignments.size() > 0 || filterCriteriaAssignments.size() > 0) {
					throw new ValidationException("Parameter is declared and set but does not contribute to the return statement.");
				}
			}
		}

		
		// Check assignments are valid
		Map<String, VariableTypes> varNameToTypes = this.getForClauseVariableDeclarations();
		
		filterCriteriaAssignments = clauses.stream().filter(w -> w.getFunctionParameter() == null).map(w-> w.getVariableName()).collect(Collectors.toCollection(HashSet::new));
		
		for(String whereVariableAssignment : filterCriteriaAssignments) {
			VariableTypes vt = varNameToTypes.get(whereVariableAssignment);
			if(!vt.isAcceptAssignment()) {
				throw new ValidationException("Invalid assignment in where clause.");
			}
		}
		
		validateTypeFunctions();
	}


	private void validateTypeFunctions() {
		Map<String, VariableTypes> varNameAndType = this.getForClauseVariableAndTypes();
		Set<W> functionApplications = this.whereClause.getClauses().stream().filter(w->w.getFunctionParameter() != null).collect(Collectors.toSet());
		for(W function : functionApplications) {
			TypeFunction typeFunction = TypeFunctions.fromString(function.getFunctionName()).getTypeFunction();
			if(!typeFunction.isValid(varNameAndType.get(function.getVariableName()), varNameAndType.get(function.getFunctionParameter()))) {
				throw new ValidationException("Incorrect Type Function application.");
			}
		}
	}

	private Set<String> validateForClause() {
		// Validate For Clause
		Set<F> clauses = this.getForClause().getClauses();
		List<String> variables = clauses.stream().filter(f -> f.getVariableAsString() != null).map(f -> f.getVariableAsString()).collect(Collectors.toList());
		Set<String> variablesAsSet = clauses.stream().map(f -> f.getVariableAsString()).collect(Collectors.toCollection(HashSet::new));
		// Doing the above is actually quite inefficient since we are going through the Set twice
		// We are doing it here to explore the use of lambdas in Java
		if(variablesAsSet.size() < variables.size()) {
			throw new ValidationException("Duplicate Parameters in for clause.");
		}
		Set<String> parameters = clauses.stream().filter(f -> f.getParameterAsString() != null).map(f -> f.getParameterAsString()).collect(Collectors.toCollection(HashSet::new));
		for(String parameter : parameters) {
			if(!variables.contains(parameter)) {
				throw new ValidationException("Undeclared variable used as parameter in for clause.");
			}
		}
		
		// Functions in for clause are only applicable to words
		Map<String, VariableTypes> varNameToTypes = this.getForClauseVariableDeclarations();
		Set<F> assignments = clauses.stream().filter(f -> f.getFunctionName() != null).collect(Collectors.toSet());
		for(F varAssignment : assignments) {
			String varName = varAssignment.getParameterAsString();
			if(!VariableTypes.WORD.equals(varNameToTypes.get(varName))) {
				throw new ValidationException("Function is only applicable to Words.");
			}
		}
		
		return variablesAsSet;
	}
	
	private Map<String, VariableTypes> getForClauseVariableDeclarations() {
		Map<String, VariableTypes> varNameToTypes = new HashMap<String, VariableTypes>();
		for(ForClauseType forClauseType : this.forClause.getClauses()) {
			if(forClauseType.getFunctionName() == null) {
				varNameToTypes.put(forClauseType.getVariableAsString(), forClauseType.getVariableTypes(forClauseType.getVariableAsString()));
			}
		}
		
		return varNameToTypes;
	}
	
	private Map<String, VariableTypes> getForClauseVariableAndTypes() {
		Map<String, VariableTypes> varNameToTypes = new HashMap<String, VariableTypes>();
		for(ForClauseType<F> forClauseType : this.forClause.getClauses()) {
			if(forClauseType.getFunctionName() == null) {
				varNameToTypes.put(forClauseType.getVariableAsString(), forClauseType.getVariableTypes(forClauseType.getVariableAsString()));
			} else {
				VariableAssignment va = (VariableAssignment) forClauseType;
				varNameToTypes.put(forClauseType.getVariableAsString(), va.getInferredType());
			}
		}
		
		return varNameToTypes;
	}

	public W findParameterValue(String argument) {
		W parameterValue = null;
		Set<W> whereClauses = this.whereClause.getClauses();
		for(W w : whereClauses) {
			if(w.getFunctionName() == null) {
				if(w.getVariableName().equals(argument)) {
					parameterValue = w;
					break;
				}
			}
		}
		
		return parameterValue;
	}

	public Set<VariableAssignment> allForClauseFunctions() {
		return this.forClause.getAllFunctions().stream().map(f -> VariableAssignment.class.cast(f)).collect(Collectors.toSet()); 
	}

	public void fillInConstraint(Map<String, Object> varToResult) {
		Set<W> wordConstraints = this.whereClause.getClauses().stream().filter(w->w.getFunctionParameter() == null).collect(Collectors.toSet());
		for(W w : wordConstraints) {
			varToResult.put(w.getVariableName(), w.getVariableValue());
		}
		
	}
}
