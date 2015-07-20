package edu.ucsd.xmlparser.entity;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class NameEntityPhraseNode {
	@GraphId 
	private Long id;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "ne_phrase")
	private String phrase;
	
	private String neType;
	
	private Long documentId; 
	
	private Long sectionId;
	
	private Long sentenceId;
	
	@SuppressWarnings("unused")
	private NameEntityPhraseNode() {
	}
	
	public NameEntityPhraseNode(String phrase, String neType, Long documentId, Long sectionId, Long sentenceId) {
		this.phrase = phrase;
		this.neType = neType;
		this.documentId = documentId;
		this.sectionId = sectionId;
		this.sentenceId = sentenceId;
	}

	public Long getId() {
		return id;
	}

	public String getPhrase() {
		return phrase;
	}

	public String getNeType() {
		return neType;
	}
	
	public Long getDocumentId() {
		return documentId;
	}

	public Long getSectionId() {
		return sectionId;
	}

	public Long getSentenceId() {
		return sentenceId;
	} 
}
