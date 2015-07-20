package edu.ucsd.xmlparser.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

/**
 * Pertains to Section Level Information
 * 
 * @author rogertan
 *
 */
@NodeEntity
public class CValueSectionNode {
	@GraphId 
	private Long id;
	
	@Indexed(indexType=IndexType.FULLTEXT, indexName = "cvalue_text")
	private String text;
	
	private Double cValue;
	
	private Integer frequency;

	private String referenceType;
	
	private Long sectionId;
	private Set<Long> sentenceIds = new HashSet<Long>();
	
	@SuppressWarnings("unused")
	private CValueSectionNode() {
	}
	
	public CValueSectionNode(String text, Integer frequency, Double cValue, Long sectionId, Set<Long> sentenceIds) {
		this.text = text;
		this.frequency = frequency;
		this.cValue = cValue;
		this.referenceType = ReferenceType.SECTION.getFriendlyString();
		this.sectionId = sectionId;
		this.sentenceIds.addAll(sentenceIds);
	}

	public Long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public Double getcValue() {
		return cValue;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public Long getSectionId() {
		return sectionId;
	}

	public Set<Long> getSentenceIds() {
		return sentenceIds;
	}
}
