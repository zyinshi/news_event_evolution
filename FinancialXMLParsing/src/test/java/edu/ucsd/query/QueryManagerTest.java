package edu.ucsd.query;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.ucsd.grammar.ParseException;

public class QueryManagerTest {
	private QueryManager queryManager;
	
	@Before
	public void setUp() {
		queryManager = new QueryManager();
	}
	
	@Test
	public void testFindShortestPhrase() throws ParseException {
		QueryResult<String> result = queryManager.executeQuery(Query.createQuery("for w:Word, p = shortest_phrase_starting_with(w) where w = 'Walt' return p").getParsedQuery(), String.class);
		Assert.assertEquals(result.getResult(), "Walt Disney");
	}
	
	@Test
	public void testSentenceShortestPhrase() throws ParseException {
		QueryResult<Set> result = queryManager.executeQuery(Query.createQuery("for s:Sentence, w:Word, p = shortest_phrase_starting_with(w) where w = 'Walt' and s.contains(p) return s").getParsedQuery(), Set.class);
		Assert.assertEquals(6, result.getResult().size());
		for(String res : (Set<String>)result.getResult()) {
			System.out.println(res);
		}
	}
	
	@Test
	public void testDocumentShortestPhrase() throws ParseException {
		QueryResult<Set> result = queryManager.executeQuery(Query.createQuery("for d:Document, w:Word, p = shortest_phrase_starting_with(w) where w = 'Walt' and d.contains(p) return d").getParsedQuery(), Set.class);
		Assert.assertEquals(1, result.getResult().size());
		for(String res : (Set<String>)result.getResult()) {
			System.out.println(res);
		}
	}
	
	@Test
	public void testDocumentShortestPhraseContext() throws ParseException {
		QueryResult<Set> result = queryManager.executeQuery(Query.createQuery("for d:Document, w:Word, p = shortest_phrase_starting_with(w) where w = ('Walt', 'Person') and d.contains(p) return d").getParsedQuery(), Set.class);
		Assert.assertEquals(1, result.getResult().size());
		for(String res : (Set<String>)result.getResult()) {
			System.out.println(res);
		}
	}
	
	@Test
	public void testReturnWord() throws ParseException {
		QueryResult<String> result = queryManager.executeQuery(Query.createQuery("for w:Word where w = 'Walt' return w").getParsedQuery(), String.class);
		Assert.assertEquals(result.getResult(), "Walt");
	}
	
	@Test
	public void testSentenceLongestPhrase() throws ParseException {
		QueryResult<Set> result = queryManager.executeQuery(Query.createQuery("for s:Sentence, w:Word, p = longest_phrase_containing(w) where w = 'Disney' and s.contains(p) return s").getParsedQuery(), Set.class);
		Assert.assertEquals(3, result.getResult().size());
		for(String res : (Set<String>)result.getResult()) {
			System.out.println(res);
		}
	}
	
	@Test
	public void testSentenceWordNotExistent() throws ParseException {
		QueryResult<Set> result = queryManager.executeQuery(Query.createQuery("for d:Document, w:Word, p = shortest_phrase_starting_with(w) where w = 'Waltzy' and d.contains(p) return d").getParsedQuery(), Set.class);
		Assert.assertTrue(result.getResult().size() == 0);	
	}
}
