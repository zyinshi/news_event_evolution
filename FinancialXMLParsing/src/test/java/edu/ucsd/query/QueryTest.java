package edu.ucsd.query;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import edu.ucsd.grammar.ParseException;
import edu.ucsd.grammar.TokenMgrError;
import edu.ucsd.grammar.ValidationException;

public class QueryTest {
	@Rule
	public ExpectedException undeclaredParametersInForClause = ExpectedException.none();
	
	@Rule
	public ExpectedException duplicateParametersInForClause = ExpectedException.none();
	
	@Rule
	public ExpectedException invalidParameterTypeInForClause = ExpectedException.none();
	
	@Rule
	public ExpectedException undeclaredParametersInWhereClause = ExpectedException.none();
	
	@Rule
	public ExpectedException duplicateParametersInWhereClause = ExpectedException.none();
	
	@Rule
	public ExpectedException undeclaredVariableUsedAsParameterInWhereClause = ExpectedException.none();
	
	@Rule
	public ExpectedException unusedParameterInWhereClause = ExpectedException.none();
	
	@Rule
	public ExpectedException undeclaredVariableInReturnClause = ExpectedException.none();
	
	@Rule
	public ExpectedException invalidAssignment = ExpectedException.none();
	
	@Rule
	public ExpectedException incorrectTypeApplication1 = ExpectedException.none();
	
	@Rule
	public ExpectedException incorrectTypeApplication2 = ExpectedException.none();
	
	@Rule
	public ExpectedException incorrectTypeApplication3 = ExpectedException.none();
	
	@Test
	public void testValidQuery() throws ParseException {
		Query query = Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and s.contains(w) return s");
		assertTrue("Query is not null indicating successful parse.", query != null);
		Query.createQuery("for w:Word where w = 'Walt' return w");
		Query.createQuery("for w:Word, d:Document, p = shortest_phrase_starting_with(w) where w = 'Walt' and d.contains(p) return d");
		Query.createQuery("for w:Word, d:Document, p = shortest_phrase_starting_with(w) where w = ('Walt', 'Person') and d.contains(p) return d");
		Query.createQuery("for w:Word, p = shortest_phrase_starting_with(w) where w = 'Walt' return p");
	}
	
	@Test
	public void testUndeclaredParametersInForClause() throws ParseException {
		undeclaredParametersInForClause.expect(ValidationException.class);
		undeclaredParametersInForClause.expectMessage("Undeclared variable used as parameter in for clause.");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(q), s:Sentence where w = 'Walt' and s.contains(w) return s");
	}
	
	@Test
	public void testDuplicateParametersInForClause() throws ParseException {
		duplicateParametersInForClause.expect(ValidationException.class);
		duplicateParametersInForClause.expectMessage("Duplicate Parameters in for clause.");
		Query.createQuery("for w:Word , w:Sentence, p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and s.contains(w) return s");
	}
	
	@Test
	public void testInvalidParameterTypeInForClause() throws ParseException {
		invalidParameterTypeInForClause.expect(ValidationException.class);
		invalidParameterTypeInForClause.expectMessage("Function is only applicable to Words.");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(s), s:Sentence where w = 'Walt' and s.contains(w) return s");		
	}
	
	@Test
	public void testUndeclaredVariablesInWhereClause() throws ParseException {
		undeclaredParametersInWhereClause.expect(ValidationException.class);
		undeclaredParametersInWhereClause.expectMessage("Undeclared variable in where clause");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and w1 = 'Disney' and s.contains(w) return s");
	}
	
	@Test
	public void testDuplicateVariablesInWhereClause() throws ParseException {
		duplicateParametersInWhereClause.expect(ValidationException.class);
		duplicateParametersInWhereClause.expectMessage("Duplicate Parameters in where clause.");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and w = 'Disney' and s.contains(w) return s");
	}
	
	@Test
	public void testUndeclaredParametersInWhereClause() throws ParseException {
		undeclaredVariableUsedAsParameterInWhereClause.expect(ValidationException.class);
		undeclaredVariableUsedAsParameterInWhereClause.expectMessage("Undeclared variable used as parameter in where clause.");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and s.contains(w1) return s");
	}
	
	@Test(expected=TokenMgrError.class)
	public void testParameterValueMustBeSingleStringInWhereClause() throws ParseException {
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt Disney' and s.contains(w) return s");		
	}
	
	@Test
	public void testUnusedParameterInWhereClause1() throws ParseException {
		unusedParameterInWhereClause.expect(ValidationException.class);
		unusedParameterInWhereClause.expectMessage("Parameter is declared and set but does not contribute to the return statement.");
		Query.createQuery("for w:Word , w1:Word, p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and w1 = 'Disney' and s.contains(w) return s");			
	}
	
	@Test
	public void testUnusedParameterInWhereClause2() throws ParseException {
		unusedParameterInWhereClause.expect(ValidationException.class);
		unusedParameterInWhereClause.expectMessage("Parameter is declared but does not contribute to the where and return statements.");
		Query.createQuery("for w:Word , w1:Word, p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and s.contains(w) return w");			
	}
	
	@Test
	public void testInvalidAssigmentInWhereClause() throws ParseException {
		invalidAssignment.expect(ValidationException.class);
		invalidAssignment.expectMessage("Invalid assignment in where clause.");
		Query.createQuery("for s:Sentence where s = 'Walt' return s");
	}
	
	@Ignore
	@Test
	public void testInvalidFunctionCallInWhereClause() throws ParseException {
		// Calling the contains function on a w 
		// Query query = Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and w.contains(s) return s");
	}
	
	@Test
	public void testInvalidContainsInWhereClause1() throws ParseException {
		// Document can contain Sentence, Phrase and Word, but not the other way around
		incorrectTypeApplication1.expect(ValidationException.class);
		incorrectTypeApplication1.expectMessage("Parameter is declared and set but does not contribute to the return statement.");
		Query.createQuery("for w:Word, s:Sentence where w = 'Walt' and s.contains(s) return s");
	}
	
	@Test(expected=ParseException.class)
	public void testInvalidContainsInWhereClause2() throws ParseException {
		// Document can contain Sentence, Phrase and Word, but not the other way around
		/*
		incorrectTypeApplication2.expect(ValidationException.class);
		incorrectTypeApplication2.expectMessage("Incorrect Type Function application.");
		*/
		Query.createQuery("for w:Word where w.contains(w) return w");
	}
	
	@Test
	public void testInvalidContainsInWhereClause3() throws ParseException {
		// Document can contain Sentence, Phrase and Word, but not the other way around
		incorrectTypeApplication3.expect(ValidationException.class);
		incorrectTypeApplication3.expectMessage("Duplicate Parameters in where clause.");
		Query.createQuery("for s:Sentence, w:Word where w = 'Walt' and w.contains(s) return w");
	}
	
	
	@Test
	public void testUndeclaredVariableInReturnClause() throws ParseException {
		this.undeclaredVariableInReturnClause.expect(ValidationException.class);
		this.undeclaredVariableInReturnClause.expectMessage("Undeclared variable in return clause.");
		Query.createQuery("for w:Word , p = shortest_phrase_starting_with(w), s:Sentence where w = 'Walt' and s.contains(w) return s1");
	}
}
