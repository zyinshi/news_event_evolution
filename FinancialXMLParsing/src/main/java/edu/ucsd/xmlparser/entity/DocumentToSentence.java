package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_SENTENCE")
public class DocumentToSentence {
	@GraphId
	private Long id; 
	
    @StartNode 
    private Document document;
    
    @EndNode
    private Sentence sentence;
    
    @SuppressWarnings("unused")
	private DocumentToSentence() {
    }
    
    public DocumentToSentence(Document document, Sentence sentence) {
    	if (sentence == null) {
    		throw new IllegalArgumentException("Sentence can not be null.");
    	}
    	
    	if (document == null) {
    		throw new IllegalArgumentException("Sentence can only be associated with Document");
    	}
    	
    	this.sentence = sentence;
    	this.document = document;
    }
}
