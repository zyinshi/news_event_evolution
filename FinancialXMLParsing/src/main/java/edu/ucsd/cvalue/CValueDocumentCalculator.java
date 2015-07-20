package edu.ucsd.cvalue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.xmlparser.entity.CValueCollectionNode;
import edu.ucsd.xmlparser.entity.CValueDocumentNode;
import edu.ucsd.xmlparser.repository.CValueRepository;

public class CValueDocumentCalculator {
	@Inject
	private CValueRepository cValueRepository;
	
	@Inject
	private Neo4jTemplate template;
	
	@Transactional
	public void computeCollectionLevelCValue() {
		Map<String, Integer> termAndFrequency = new HashMap<String, Integer>();
		List<CValueDocumentNode> documentNodes = cValueRepository.getAllDocumentNodes();
		for(CValueDocumentNode node : documentNodes) {
			Integer frequency = termAndFrequency.getOrDefault(node.getText(), 0);
			termAndFrequency.put(node.getText(), node.getFrequency() + frequency);
		}
		
		List<CValueData> cValueDatas = new ArrayList<CValueData>();
		for(String term : termAndFrequency.keySet()) {
			cValueDatas.add(new CValueData(term, termAndFrequency.get(term), new StringTokenizer(term).countTokens()));
		}
		
		Collections.sort(cValueDatas, new Comparator<CValueData>() {
			@Override
			public int compare(CValueData o1, CValueData o2) {
				return new Integer(o2.getPhraseLength()).compareTo(new Integer(o1.getPhraseLength()));
			}
		});
		CValueCalculator.calculate(cValueDatas);
		System.out.println("Number of Terms : " + cValueDatas.size());
		
		Collections.sort(cValueDatas, new Comparator<CValueData>() {
			@Override
			public int compare(CValueData o1, CValueData o2) {
				return o2.getCValue().compareTo(o1.getCValue());
			}
		});
		
		Map<String, CValueData> cValueMap = new HashMap<String, CValueData>();
		for(CValueData data : cValueDatas) {
			cValueMap.put(data.getPhrase(), data);
		}
		
		for(String term : termAndFrequency.keySet()) {
			Integer frequency = termAndFrequency.get(term);
			CValueCollectionNode node = new CValueCollectionNode(term, cValueMap.get(term).getCValue(), frequency);
			template.save(node);
		}
	}
}
