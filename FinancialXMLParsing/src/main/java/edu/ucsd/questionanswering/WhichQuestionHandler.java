package edu.ucsd.questionanswering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.wordnet.LexicalUtility;
import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.PhraseTypes;
import edu.ucsd.xmlparser.entity.Word;
import edu.ucsd.xmlparser.repository.DocumentRepository;
import edu.ucsd.xmlparser.repository.SentenceRepository;
import edu.ucsd.xmlparser.util.Neo4jUtils;

public class WhichQuestionHandler implements QuestionHandler, ApplicationContextAware {
	private static Map<String, String> sentenceFormTypeToHandler = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(WhichQuestionHandler.class);
	
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
	
	private ApplicationContext context;
	
	@Override
	@Transactional
	public Answer answerQuestion(List<ParsedWord> parsedQuestion) {
		Answer answer = new NoAnswer();
		
		// This is what we are looking for
		String answerNameEntityType = parsedQuestion.get(1).getLemma();
		if(!NeTags.isValid(answerNameEntityType))  {
			throw new IllegalArgumentException("Unsupported argument to question starting with which : " + answerNameEntityType);
		}
		
		// Look for Actor
		String actor = lookForSubject(parsedQuestion, 2);
		Long documentId = lookForMostProbablyDocument(actor);
		
		List<Answer> answers = new ArrayList<Answer>();
		
		if(documentId != null) {
			// Look for Verb and Noun Equivalents
			Set<String> verbAndNounEquivalents = lookForVerbAndNounEquivalents(parsedQuestion, 2);
			if(verbAndNounEquivalents.size() > 0) {
				// We look for words that 
				List<Word> words = sentenceRepository.findWords(documentId, verbAndNounEquivalents);
				if(logger.isDebugEnabled()) {
					logger.debug("Searching for occurence of the following verb and noun equivalents:");
				}
				for(Word word : words) {
					if(logger.isDebugEnabled()) {
						logger.debug(word.toString());
					}
					String handlerName = sentenceFormTypeToHandler.get(word.getPosTag());
					if(handlerName != null) {
						SentenceFormHandler sentenceHandler = SentenceFormHandler.class.cast(this.context.getBean(handlerName));
						answers.add(sentenceHandler.handleWord(word, NeTags.fromString(answerNameEntityType)));
					} else {
						logger.info("No handler for : " + word);
					}
				} 
			}
		}
		
		if(answers.size() > 0) {
			Set<String> answerRaws = new HashSet<String>();
			for(Answer ans : answers) {
				if(!ans.isNoAnswer()) {
					if(logger.isDebugEnabled()) {
						logger.debug("Raw Answer: " + ans.asText());
					}
					if(!ans.asText().trim().equals("")) {
						answerRaws.add(ans.asText());
					}
				}
			}
			answer = new SetAnswer(answerRaws);
		}
		
		return answer;
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

	private String lookForSubject(List<ParsedWord> parsedQuestion, int start) {
		String subject = null;
		
		// First we look at the words tagged by the Stanford Parser as an organization or person
		for(int i = start; i < parsedQuestion.size(); i++) {
			ParsedWord pw = parsedQuestion.get(i);
			if(NeTags.isOrganizationOrPerson(pw.getNeTag())) {
				if(subject == null) {
					subject = pw.getWord();
				} else {
					subject += " ";
					subject += pw.getWord();
				}
			}
		}
		
		// If the Stanford Parser was either unable to find words tagged as an organization/person
		// we look for the next best thing which are words tagged as NNP
		if(subject == null) {
			for(int i = start; i < parsedQuestion.size(); i++) {
				ParsedWord pw = parsedQuestion.get(i);
				if("NNP".equals(pw.getPosTag())) {
					if(subject == null) {
						subject = pw.getWord();
					} else {
						subject += " ";
						subject += pw.getWord();
					}
				}
			}	
		} 
		
		return subject.trim();
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.context = arg0;
	}
}
