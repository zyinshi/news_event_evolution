package edu.ucsd.xmlparser.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class CValueDocumentNode {
	@GraphId 
	private Long id;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "cvalue_text")
	private String text;
	
	private Double cValue;
	
	private Integer frequency;

	private String referenceType;

	private Long documentId;
	
	private Set<Long> sectionIds = new HashSet<Long>();
	
	
	@SuppressWarnings("unused")
	private CValueDocumentNode() {
	}

	public CValueDocumentNode(String text, Integer frequency, Double cValue, Long documentId, Set<Long> sectionIds) {
		super();
		this.text = text;
		this.cValue = cValue;
		this.frequency = frequency;
		this.referenceType = ReferenceType.DOCUMENT.getFriendlyString();
		this.documentId = documentId;
		this.sectionIds = sectionIds;
	}

	public Long getId() {
		return id;
	}


	public String getText() {
		return text;
	}


	public Double getCValue() {
		return cValue;
	}


	public Integer getFrequency() {
		return frequency;
	}


	public String getReferenceType() {
		return referenceType;
	}


	public Long getDocumentId() {
		return documentId;
	}


	public Set<Long> getSectionIds() {
		return sectionIds;
	}
}
