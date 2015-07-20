package edu.ucsd.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.grammar.ParsedQuery;
import edu.ucsd.grammar.VariableAssignment;
import edu.ucsd.grammar.VariableTypes;
import edu.ucsd.query.function.Contains;
import edu.ucsd.query.function.Function;
import edu.ucsd.query.function.LongestPhrase;
import edu.ucsd.query.function.ShortestPhrase;
import edu.ucsd.system.SystemApplicationContext;
import edu.ucsd.xmlparser.entity.Sentence; 

public class QueryManager {
	public <T> QueryResult<T> executeQuery(ParsedQuery<?, ?> parsedQuery, Class<T> type) {
		QueryManagerInner innerManager = (QueryManagerInner)SystemApplicationContext.getApplicationContext().getBean("queryManagerInner");
		return innerManager.executeQuery(parsedQuery, type);
	}
	
	private interface QueryManagerInner {
		<T> QueryResult<T> executeQuery(ParsedQuery<?, ?> parsedQuery, Class<T> type);
	}
	
	@SuppressWarnings("unused")
	private static class QueryManagerInnerImpl implements QueryManagerInner {
		@Override
		@Transactional
		public <T> QueryResult<T> executeQuery(ParsedQuery<?, ?> parsedQuery,
				Class<T> type) {
			QueryResult<T> result = null;
			
			// This is where we are going to store the result of execution of various intermediate function
			Map<String, Object> varToResult = new HashMap<String, Object>();
			parsedQuery.fillInConstraint(varToResult);
			
			// We execute each function from the for clause to the where clause
			// Once all functions have been executed, it must be the case that we have the final result
			Set<VariableAssignment> variableAssignments = parsedQuery.allForClauseFunctions();
			
			Set<Sentence> containingSentences = null;
			
			// NEED REFACTORING to make adding new functions easier
			// The problem is we don't know what functions will be there in the future
			for(VariableAssignment va : variableAssignments) {
				if(va.getFunctionName().equals(ShortestPhrase.FUNCTION_NAME)) {
					@SuppressWarnings("unchecked")
					Function<VariableAssignment, ShortestPhrase.ShortestPhraseResult> sp = (Function<VariableAssignment, ShortestPhrase.ShortestPhraseResult>)SystemApplicationContext.getApplicationContext().getBean("shortestPhraseFunction");
					ShortestPhrase.ShortestPhraseResult spr = sp.evaluate(va, parsedQuery);
					varToResult.put(va.getVariableName(), spr.getText());
					containingSentences = spr.getContainingSentences();
				} else if(va.getFunctionName().equals(LongestPhrase.FUNCTION_NAME)) {
					@SuppressWarnings("unchecked")
					Function<VariableAssignment, LongestPhrase.LongestPhraseResult> lp = (Function<VariableAssignment, LongestPhrase.LongestPhraseResult>)SystemApplicationContext.getApplicationContext().getBean("longestPhraseFunction");
					LongestPhrase.LongestPhraseResult lpr = lp.evaluate(va, parsedQuery);
					varToResult.put(va.getVariableName(), lpr.getText());
					containingSentences = lpr.getContainingSentences();
				} else {
					throw new IllegalArgumentException("Unrecognized function name");
				}
			}
			
			// See if we've already computed the return value if yes
			Object finalResult = varToResult.get(parsedQuery.getReturnClause().getVariableName());
			if(finalResult != null) {
				result = new QueryResult<T>(finalResult, type);
			} else {
				// We need to compute contains relationship
				VariableTypes types = parsedQuery.getForClause().getVariableTypes(parsedQuery.getReturnClause().getVariableName());
				Contains contains = (Contains)SystemApplicationContext.getApplicationContext().getBean(types.getContainsFunction());
				result = new QueryResult<T>(contains.contains(containingSentences), type);
			}
			
			return result;
		}
		
	}
}
