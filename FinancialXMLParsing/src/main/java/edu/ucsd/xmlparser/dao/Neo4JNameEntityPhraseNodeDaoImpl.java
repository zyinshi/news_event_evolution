package edu.ucsd.xmlparser.dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;

public class Neo4JNameEntityPhraseNodeDaoImpl implements
		NameEntityPhraseNodeDao {
	private final static String COMMON_MATCH = "match (n:NameEntityPhraseNode) where ";
	private final static String COMMON_RETURN = " return n.sentenceId as sid";
	@Inject
	private GraphDatabaseService graphDatabaseService;
	
	private ExecutionEngine executionEngine;
	
	@Override
	public Set<Long> getSentenceIdsContainingNameEntity(Long documentId,
			String nameEntity) {
		ExecutionEngine engine = getExecutionEngine();
		StringBuilder sb = new StringBuilder();
		sb.append(COMMON_MATCH);
		sb.append("n.documentId = ");
		sb.append(documentId);
		sb.append(" and n.phrase =~ '(?i).*");
		sb.append(nameEntity);
		sb.append(".*'");
		sb.append(COMMON_RETURN);
		
		Set<Long> finalResult = new HashSet<Long>();
		
		ExecutionResult result = engine.execute(sb.toString());
		Iterator<Long> n_column = result.columnAs( "sid" );
		while(n_column.hasNext()) {
			finalResult.add(n_column.next());
		}
		
		return finalResult;
	}

	@Override
	public Set<Long> getSentenceIdsContainingNameEntity(String nameEntity) {
		ExecutionEngine engine = getExecutionEngine();
		StringBuilder sb = new StringBuilder();
		sb.append(COMMON_MATCH);
		sb.append("n.phrase =~ '(?i).*");
		sb.append(nameEntity);
		sb.append(".*'");
		sb.append(COMMON_RETURN);
		
		Set<Long> finalResult = new HashSet<Long>();
		
		ExecutionResult result = engine.execute(sb.toString());
		Iterator<Long> n_column = result.columnAs( "sid" );
		while(n_column.hasNext()) {
			finalResult.add(n_column.next());
		}
			
		return finalResult;
	}

	private ExecutionEngine getExecutionEngine() {
		if(this.executionEngine == null) {
			this.executionEngine = new ExecutionEngine(graphDatabaseService);
		}
		return this.executionEngine;
	}
}
