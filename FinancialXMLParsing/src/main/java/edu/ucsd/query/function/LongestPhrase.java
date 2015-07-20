package edu.ucsd.query.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.grammar.ParsedQuery;
import edu.ucsd.grammar.VariableAssignment;
import edu.ucsd.grammar.WordConstraint;
import edu.ucsd.query.dao.QueryFunctionDao;
import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;
import edu.ucsd.xmlparser.entity.Sentence;
import edu.ucsd.xmlparser.entity.Word;

public class LongestPhrase implements
		Function<VariableAssignment, LongestPhrase.LongestPhraseResult> {
	public final static String FUNCTION_NAME = "longest_phrase_containing";
	
	private final static Logger logger = LoggerFactory.getLogger(LongestPhrase.class);
	
	private Map<Integer, Set<Sentence>> lengthToSentences = new HashMap<Integer, Set<Sentence>>();
	
	private int currentLongestLength = 0;
	
	@Inject
	private QueryFunctionDao queryFunctionDao;
	
	@Inject
	private Neo4jTemplate template;
	
	public class LongestPhraseResult {
		private String text;
		// We include the containing sentence to speed up certain retrieval results
		private Set<Sentence> containingSentences;
		
		public LongestPhraseResult(String text, Set<Sentence> containingSentences) {
			this.text = text;
			this.containingSentences = containingSentences;
		}

		public String getText() {
			return text;
		}
		
		public Set<Sentence> getContainingSentences() {
			return this.containingSentences;
		}
	}

	@Override
	@Transactional
	public LongestPhraseResult evaluate(VariableAssignment variableAssignment, ParsedQuery query) {
		if(!variableAssignment.getFunctionName().equals(FUNCTION_NAME)) {
			throw new IllegalArgumentException("Wrong function argument"); 
		}
		
		WordConstraint parameter = (WordConstraint)query.findParameterValue(variableAssignment.getArgument());
		List<Word> words = null;
		if(parameter.getVariableContext() == null) {
			words = queryFunctionDao.getWord(parameter.getVariableValue());
		} else {
			words = queryFunctionDao.getWord(parameter.getVariableValue(), parameter.getVariableContext().toUpperCase());
		}
		String longestPhrase = lookForLongestPhrase(words);
		return new LongestPhraseResult(longestPhrase, lengthToSentences.get(currentLongestLength));
	}

	private String lookForLongestPhrase(List<Word> words) {
		String longestPhrase = null;
		
		for(Word word : words) {
			List<String> sb = new ArrayList<String>();
			String currentPosTag = word.getPosTag();
			Node node = template.getNode(word.getId());
			buildPhraseWithConsecutiveTag(sb, node, currentPosTag);
			
			List<String> prefix = new ArrayList<String>();
			buildPrefixWithConsecutiveTag(prefix, node, currentPosTag);
			
			int sentenceLength = prefix.size() + sb.size();
			if(currentLongestLength == 0 || sentenceLength > currentLongestLength) {
				StringBuilder stringBuilder = new StringBuilder();
				for(String s: prefix) {
					stringBuilder.append(s + " ");
				}
			
				for(String s : sb) {
					stringBuilder.append(s + " ");
				}

				longestPhrase = stringBuilder.toString().trim();	
				currentLongestLength = sentenceLength;
			}
			
			Set<Sentence> sentencesWithTheSameLength = this.lengthToSentences.get(sentenceLength);
			if(sentencesWithTheSameLength == null) {
				sentencesWithTheSameLength = new HashSet<Sentence>();
				this.lengthToSentences.put(sentenceLength, sentencesWithTheSameLength);
			} 
			sentencesWithTheSameLength.add(this.getContainingSentence(node));	
		}
		
		return longestPhrase;
	}
	
	private void buildPrefixWithConsecutiveTag(List<String> prefix, Node word,
			String posTag) {
		if(word.hasProperty("posTag") && word.getProperty("posTag").equals(posTag)) {
			prefix.add((String)word.getProperty("text"));
		} else {
			prefix.remove(0); // We need to remove this since we already get this from the suffix
			Collections.reverse(prefix);
			return;
		}
		
		Iterable<Relationship> rels = word.getRelationships(Direction.INCOMING, ApplicationRelationshipType.NEXT_WORD);
		if(rels.iterator().hasNext()) {
			buildPrefixWithConsecutiveTag(prefix, rels.iterator().next().getStartNode(), posTag);
		} 
	}

	private Sentence getContainingSentence(Node word) {
		Iterable<Relationship> rels = word.getRelationships(Direction.INCOMING, ApplicationRelationshipType.HAS_WORD);
		return this.template.findOne(rels.iterator().next().getOtherNode(word).getId(), Sentence.class);
	}
	
	private void buildPhraseWithConsecutiveTag(List<String> sb, Node word, String posTag) {
		if(word.getProperty("posTag").equals(posTag)) {
			sb.add((String)word.getProperty("text"));
		} else {
			return;
		}
		
		Iterable<Relationship> rels = word.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.NEXT_WORD);
		if(rels.iterator().hasNext()) {
			buildPhraseWithConsecutiveTag(sb, rels.iterator().next().getEndNode(), posTag);
		}
	}
}
