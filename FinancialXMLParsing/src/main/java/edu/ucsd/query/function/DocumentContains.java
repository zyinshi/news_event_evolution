package edu.ucsd.query.function;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;
import edu.ucsd.xmlparser.entity.Document;
import edu.ucsd.xmlparser.entity.Sentence;

public class DocumentContains implements Contains {
	@Inject
	private Neo4jTemplate template;
	
	@Override
	public Set<String> contains(Set<Sentence> sentences) {
		if(sentences == null || sentences.size() == 0) {
			return new HashSet<String>();
		}
		
		Set<String> documents = new HashSet<String>();
		for(Sentence sentence : sentences) {
			String title = getContainingDocument(sentence);
			documents.add(title);
		}
		return documents;
	}

	private String getContainingDocument(Sentence sentence) {
		String result = null;
		Node sentenceNode = template.getNode(sentence.getId());
		Iterator<Relationship> rels = sentenceNode.getRelationships(Direction.INCOMING).iterator();
		while(rels.hasNext()) {
			Relationship rel = rels.next();
			if(ApplicationRelationshipType.HAS_SENTENCE.name().equals(rel.getType().name())) {
				result = (String)rel.getStartNode().getProperty("title");
				break;
			}
		}
		return result;
	}

}
