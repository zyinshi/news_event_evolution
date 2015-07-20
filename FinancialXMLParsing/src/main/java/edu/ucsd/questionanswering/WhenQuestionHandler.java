package edu.ucsd.questionanswering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import edu.ucsd.wordnet.LexicalUtility;
import edu.ucsd.xmlparser.dao.NameEntityPhraseNodeDao;
import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;
import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.PhraseTypes;
import edu.ucsd.xmlparser.entity.Word;
import edu.ucsd.xmlparser.repository.DocumentRepository;
import edu.ucsd.xmlparser.repository.SentenceRepository;
import edu.ucsd.xmlparser.util.Neo4jUtils;

public class WhenQuestionHandler implements QuestionHandler {
	private static Map<String, String> sentenceFormTypeToHandler = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(WhenQuestionHandler.class);
	
	static {
		sentenceFormTypeToHandler.put("NNS", "nnsSentenceFormHandler");
		sentenceFormTypeToHandler.put("NN", "nnsSentenceFormHandler");
		sentenceFormTypeToHandler.put("VBD","verbSentenceFormHandler");
		sentenceFormTypeToHandler.put("VB","verbSentenceFormHandler");
		sentenceFormTypeToHandler.put("VBG","verbSentenceFormHandler");
		sentenceFormTypeToHandler.put("VBN","verbSentenceFormHandler");
		sentenceFormTypeToHandler.put("VBP","verbSentenceFormHandler");
	}
	
	@Inject
	private DocumentRepository documentRepository;
	
	@Inject
	private SentenceRepository sentenceRepository;
	
	@Inject
	private NameEntityPhraseNodeDao neRepository;
	
	@Inject
	private Neo4jTemplate template;
	
	@Override
	@Transactional
	public Answer answerQuestion(List<ParsedWord> parsedQuestion) {
		Answer answer = new NoAnswer();
			
		// Look for Actor
		String actor = lookForActor(parsedQuestion, 2);
		Long documentId = lookForMostProbablyDocument(actor);
		
		Set<String> answers = new HashSet<String>();
		
		if(documentId != null) {
			// Look for Verb and Noun Equivalents
			Set<String> verbAndNounEquivalents = lookForVerbAndNounEquivalents(parsedQuestion, 2); 
			if(verbAndNounEquivalents.size() > 0) {
				String object = lookForSentenceObject(parsedQuestion, 3);
				// We look for words that 
				Set<Long> sentenceIds = sentenceRepository.findSentenceIds(documentId, verbAndNounEquivalents);
				logger.debug("Number of candidate sentences: " + sentenceIds.size());
				Set<Long> neSentenceIds = neRepository.getSentenceIdsContainingNameEntity(documentId, object);
				logger.debug("Number of ne candidate sentences: " + neSentenceIds.size());
				Set<Long> trueCandidateSentenceIds = intersection(sentenceIds, neSentenceIds);
				logger.debug("Number of true candidate sentence ids: " + trueCandidateSentenceIds.size());
				if(trueCandidateSentenceIds.size() > 0) {
					Set<Word> words = sentenceRepository.findWordsWithSentenceIdsAndWords(trueCandidateSentenceIds, verbAndNounEquivalents);
					logger.debug("Number of words: " + words.size());
					for(Word word : words) {
						Node node = template.getNode(word.getId());
						Iterator<Relationship> rels = node.getRelationships(Direction.OUTGOING, ApplicationRelationshipType.WORD_DEPENDENCY).iterator();
						String dateString = "";
						String company = "";
						while(rels.hasNext()) {
							Relationship rel = rels.next();
							if(logger.isDebugEnabled()) {
								logger.info(rel.getProperty("dependency").toString());
							}
							if(rel.getProperty("dependency").equals("prep_in") || rel.getProperty("dependency").equals("prep_on")) {
								if(rel.getEndNode().getProperty("neTag").equals(NeTags.DATE.name())) {
									dateString = QAUtils.getPhrase(rel.getEndNode());
								} else {
									if(logger.isDebugEnabled()) {
										logger.debug(rel.getEndNode().getProperty("text").toString() + ", " + rel.getEndNode().getProperty("neTag").toString());
									}
									break;
								}
							} else if(rel.getProperty("dependency").equals("dobj")) {
								company = QAUtils.getPhrase(rel.getEndNode());
							} else if(rel.getProperty("dependency").equals("prep_of")) {
								company = QAUtils.getPhrase(rel.getEndNode());
							}
						}
						
						if(!dateString.equals("")) {
							if(company.contains(object)) {
								answers.add(dateString);
							}
						}
					}
				}
			}
		}
		
		if(answers.size() > 0) {
			answer = new SetAnswer(answers);
		}
		
		return answer;
	}

	private Set<Long> intersection(Set<Long> sentenceIds,
			Set<Long> neSentenceIds) {
		Set<Long> intersect = new HashSet<Long>();
		for(Long sId : sentenceIds) {
			if(neSentenceIds.contains(sId)) {
				intersect.add(sId);
			}
		}
		
		return intersect;
	}

	private String lookForSentenceObject(List<ParsedWord> parsedQuestion, int i) {
		StringBuilder sb = new StringBuilder();
		for(int index = i; index < parsedQuestion.size(); index++) {
			String posTag = parsedQuestion.get(index).getPosTag();
			if(PhraseTypes.isNNP(posTag)) {
				sb.append(parsedQuestion.get(index).getWord());
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}

	private Set<String> lookForVerbAndNounEquivalents(
			List<ParsedWord> parsedQuestion, int index) {
		Set<String> verbsAndNounEquivalents = new HashSet<String>();
		String verb = "";
		boolean likelyPresentTense = false;
		
		for(int i = index; i < parsedQuestion.size(); i++) {
			ParsedWord pw = parsedQuestion.get(i);
			if(PhraseTypes.isVB(pw.getPosTag())) {
				verb = pw.getWord();
				verbsAndNounEquivalents.add(pw.getWord());
				if(pw.isLikelyPresentTense()) {
					likelyPresentTense = true;
				}
				break;
			}
		}
		
		if(verb == null || "".equals(verb)) {
			throw new IllegalArgumentException("A Verb needs to specified for Which type questions");
		}
		
		verbsAndNounEquivalents.addAll(LexicalUtility.getNounsIncludingPluralFormsForVerb(verb));
		if(likelyPresentTense) {
			verbsAndNounEquivalents.add(LexicalUtility.getPastTense(verb));
		}
		
		return verbsAndNounEquivalents;
	}

	private Long lookForMostProbablyDocument(String actor) {
		Long docIdResult = null;
		Long count = null;
		
		Iterator<Map<String, Object>> documentIdToCount = documentRepository.getCount(Neo4jUtils.likeInput(actor)).iterator();
		while(documentIdToCount.hasNext()) {
			Map<String, Object> docIdAndCount = documentIdToCount.next();
			
			if(docIdResult == null || (Long.class.cast(docIdAndCount.get("count")) > count)) {
				docIdResult = Long.class.cast(docIdAndCount.get("documentId"));
				count = Long.class.cast(docIdAndCount.get("count"));
			} 
		}	
		
		return docIdResult;
	}

	private String lookForActor(List<ParsedWord> parsedQuestion, int start) {
		String actor = null;
		
		for(int i = start; i < parsedQuestion.size(); i++) {
			ParsedWord pw = parsedQuestion.get(i);
			if(NeTags.isOrganizationOrPerson(pw.getNeTag())) {
				if(actor == null) {
					actor = pw.getWord();
				} else {
					actor += " ";
					actor += pw.getWord();
				}
			}
		}
		
		if(actor == null) {
			for(int i = start; i < parsedQuestion.size(); i++) {
				ParsedWord pw = parsedQuestion.get(i);
				if("NNP".equals(pw.getPosTag())) {
					if(actor == null) {
						actor = pw.getWord();
					} else {
						actor += " ";
						actor += pw.getWord();
					}
				}
			}	
		} 
		
		return actor.trim();
	}
}
