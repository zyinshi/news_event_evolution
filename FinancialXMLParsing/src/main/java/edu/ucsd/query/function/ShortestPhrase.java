package edu.ucsd.query.function;

import java.util.ArrayList;
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

public class ShortestPhrase implements Function<VariableAssignment, ShortestPhrase.ShortestPhraseResult> {
	public final static String FUNCTION_NAME = "shortest_phrase_starting_with";
	
	private final static Logger logger = LoggerFactory.getLogger(ShortestPhrase.class);
	
	private final static int MIN_LENGTH = 2;
	
	private int currentShortestLength = 0;
	
	private Map<Integer, Set<Sentence>> lengthToSentences = new HashMap<Integer, Set<Sentence>>();
	
	@Inject
	private QueryFunctionDao queryFunctionDao;
	
	@Inject
	private Neo4jTemplate template;
	
	public class ShortestPhraseResult {
		private String text;
		// We include the containing sentence to speed up certain retrieval results
		private Set<Sentence> containingSentences;
		
		public ShortestPhraseResult(String text, Set<Sentence> containingSentences) {
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
	public ShortestPhraseResult evaluate(VariableAssignment variableAssignment, ParsedQuery query) {
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
		
		String shortestPhrase = lookForShortestPhrase(words);
		return new ShortestPhraseResult(shortestPhrase, lengthToSentences.get(currentShortestLength));
	}

	private String lookForShortestPhrase(List<Word> words) {
		String shortestPhrase = null;
		
		for(Word word : words) {
			List<String> sb = new ArrayList<String>();
			String currentPosTag = word.getPosTag();
			Node node = template.getNode(word.getId());
			buildPhraseWithConsecutiveTag(sb, node, currentPosTag);
			
			if(sb.size() >= MIN_LENGTH) {
				if(currentShortestLength == 0 || sb.size() < currentShortestLength) {
					StringBuilder stringBuilder = new StringBuilder();
					for(String s : sb) {
						stringBuilder.append(s + " ");
					}
					
					shortestPhrase = stringBuilder.toString().trim();
					currentShortestLength = sb.size();
				}
			}
		}
		
		return shortestPhrase;
	}
	
	private Sentence getContainingSentence(Node word) {
		Iterable<Relationship> rels = word.getRelationships(Direction.INCOMING, ApplicationRelationshipType.HAS_WORD);
		return this.template.findOne(rels.iterator().next().getOtherNode(word).getId(), Sentence.class);
	}
	
	private void buildPhraseWithConsecutiveTag(List<String> sb, Node word, String posTag) {
		if(word.getProperty("posTag").equals(posTag)) {
			sb.add((String)word.getProperty("text"));
		} else {
			Set<Sentence> sentencesWithTheSameLength = this.lengthToSentences.get(sb.size());
			if(sentencesWithTheSameLength == null) {
				sentencesWithTheSameLength = new HashSet<Sentence>();
				this.lengthToSentences.put(sb.size(), sentencesWithTheSameLength);
			} 
			sentencesWithTheSameLength.add(this.getContainingSentence(word));
			return;
		}
		
		Iterable<Relationship> rels = word.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.NEXT_WORD);
		if(rels.iterator().hasNext()) {
			buildPhraseWithConsecutiveTag(sb, rels.iterator().next().getEndNode(), posTag);
		}
	}

}
