package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="HAS_PARSE_CHILD")
public class SentenceToNonLeafParseNode {
	@GraphId
	private Long id; 
	
    @StartNode 
    private Sentence sentence;
    
    @EndNode
    private NonLeafParseNode parseNode;
    
    @SuppressWarnings("unused")
	private SentenceToNonLeafParseNode() {
    }
    
    public SentenceToNonLeafParseNode(Sentence sentence, NonLeafParseNode parseNode) {
    	if (sentence == null) {
    		throw new IllegalArgumentException("Sentence can not be null.");
    	}
    	
    	if (parseNode == null) {
    		throw new IllegalArgumentException("Sentence can only be associated with Root");
    	}
    	
    	this.sentence = sentence;
    	this.parseNode = parseNode;
    }
}