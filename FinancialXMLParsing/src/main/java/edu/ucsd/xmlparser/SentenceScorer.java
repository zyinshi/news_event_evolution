package edu.ucsd.xmlparser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.xmlparser.entity.CValueCollectionNode;
import edu.ucsd.xmlparser.entity.CValueSectionNode;
import edu.ucsd.xmlparser.entity.NameEntityPhraseNode;
import edu.ucsd.xmlparser.repository.CValueRepository;

public class SentenceScorer {
	@Inject
	private CValueRepository cValueRepository;
	
	@Inject
	private Neo4jTemplate template;
	
	@Transactional
	public void scoreSentence() {
		List<CValueCollectionNode> nodes = cValueRepository.getAllCValueCollectionNodes();
		Map<String, Double> textToCValue = convertToMap(nodes);
		List<CValueSectionNode> secNodes = cValueRepository.getAllSectionNodes();
		Map<Long, Double> sentenceIdToCombinedValue = new HashMap<Long, Double>();
		Map<String, Set<Long>> textToSentenceIds = convertToSentenceIdMapping(secNodes);
		for(String text : textToSentenceIds.keySet()) {
			for(Long sentenceId : textToSentenceIds.get(text)) {
				Double cValue = textToCValue.get(text);
				Double currentValue = sentenceIdToCombinedValue.putIfAbsent(sentenceId, cValue);
				if(currentValue != null) {
					sentenceIdToCombinedValue.put(sentenceId, cValue + currentValue);
				}
			}
		}
		
		// Add all Named Entities
		List<NameEntityPhraseNode> neNodes = cValueRepository.getAllNameEntityPhraseNodes();
		for(NameEntityPhraseNode neNode : neNodes) {
			Double value = sentenceIdToCombinedValue.putIfAbsent(neNode.getSentenceId(), 1.0);
			if(value != null) {
				sentenceIdToCombinedValue.put(neNode.getSentenceId(), value + 1.0);
			}
		}
		
		// Compute the max value and then scale by that value to get a score between 0 and 1
		Double maxValue = Collections.max(sentenceIdToCombinedValue.values());
		
		for(Long sentenceId : sentenceIdToCombinedValue.keySet()) {
			Node sentenceNode = template.getNode(sentenceId);
			sentenceNode.setProperty("score", sentenceIdToCombinedValue.get(sentenceId)/maxValue);
		}
	}

	private Map<String, Set<Long>> convertToSentenceIdMapping(
			List<CValueSectionNode> secNodes) {
		Map<String, Set<Long>> result = new HashMap<String, Set<Long>>();
		
		for(CValueSectionNode secNode : secNodes) {
			Set<Long> currentSentenceIds = result.putIfAbsent(secNode.getText(), secNode.getSentenceIds());
			if(currentSentenceIds != null) {
				currentSentenceIds.addAll(secNode.getSentenceIds());
				result.put(secNode.getText(), currentSentenceIds);
			}
		}
		
		return result;
	}

	private Map<String, Double> convertToMap(List<CValueCollectionNode> nodes) {
		Map<String, Double> result = new HashMap<String, Double>();
		for(CValueCollectionNode cValCollNode : nodes) {
			result.put(cValCollNode.getText(), cValCollNode.getCValue());
		}
		
		return result;
	}
}
