package edu.ucsd.query;

import java.io.StringReader;

import edu.ucsd.grammar.ParseException;
import edu.ucsd.grammar.ParsedQuery;
import edu.ucsd.grammar.QueryParser;

public class Query {
	private ParsedQuery parsedQuery;
	
	private Query(ParsedQuery parsedQuery) {
		this.parsedQuery = parsedQuery;
	}
	
	public ParsedQuery getParsedQuery() {
		return this.parsedQuery;
	}
	
	public static Query createQuery(String query) throws ParseException {
		if(query == null) {
			throw new IllegalArgumentException("Query string can not be null.");
		}
		
		QueryParser queryParser = new QueryParser(new StringReader(query));
		ParsedQuery parsedQuery = queryParser.parse();
		parsedQuery.validate();
		
		return new Query(parsedQuery);
	}
	
}
