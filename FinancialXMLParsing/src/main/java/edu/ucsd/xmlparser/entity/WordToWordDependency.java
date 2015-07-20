package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type="WORD_DEPENDENCY")
public class WordToWordDependency {
	@GraphId
	private Long id; 
	
    @StartNode 
    private Word startWord;
    
    @EndNode
    private Word endWord;
    
    private String dependency;
    
    @SuppressWarnings("unused")
	private WordToWordDependency() {	
    }
    
    public WordToWordDependency(Word startWord, Word endWord, String dependency) {
    	if(startWord == null) {
    		throw new IllegalArgumentException("Start Word can not be null.");
    	}
    	if(endWord == null) {
    		throw new IllegalArgumentException("End Word can not be null.");
    	}
    	if(dependency == null) {
    		throw new IllegalArgumentException("Dependency can not be null");
    	}
    	this.startWord = startWord;
    	this.endWord = endWord;
    	this.dependency = dependency;
    }

	public Long getId() {
		return id;
	}

	public Word getStartWord() {
		return startWord;
	}

	public Word getEndWord() {
		return endWord;
	}

	public String getDependency() {
		return dependency;
	}

}
