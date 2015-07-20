package edu.ucsd.questionanswering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import edu.ucsd.xmlparser.dao.NameEntityPhraseNodeDao;
import edu.ucsd.xmlparser.entity.Sentence;
import edu.ucsd.xmlparser.repository.CValueRepository;
import edu.ucsd.xmlparser.repository.SentenceRepository;

public class TellMeQuestionHandler implements QuestionHandler {
	private static Logger logger = LoggerFactory.getLogger(TellMeQuestionHandler.class);
	private static int MAX_RETURN = 3;

	@Inject
	private CValueRepository cValueRepository;
	
	@Inject
	private SentenceRepository sentenceRepository;
	
	@Inject
	private NameEntityPhraseNodeDao nePhraseNodeDao;
	
	@Override
	@Transactional
	public Answer answerQuestion(List<ParsedWord> parsedQuestion) {
		Answer answer = new NoAnswer();
		if(parsedQuestion.size() < 4 || invalidForm(parsedQuestion)) {
			return answer;
		}
		
		String aboutParameter = getParameter(parsedQuestion, 3);
		if(logger.isDebugEnabled()) {
			logger.debug("Parameter: " + aboutParameter);
		}
		Set<Long> cValueSentenceIds = cValueRepository.getSentenceIds(aboutParameter);
		if(logger.isDebugEnabled()) {
			logger.info("Number of sentence ids corresponding to cvalue text: " + cValueSentenceIds.size());
		}
		if(cValueSentenceIds.size() > 0) {
			List<Sentence> sentences = sentenceRepository.getSentenceById(cValueSentenceIds);
			sentences = sentences.stream().filter(s -> s.getScore() != null).collect(Collectors.toList());
			if(sentences.size() > 0) {
				answer = new ListAnswer(generateTopNAnswers(sentences, MAX_RETURN));
			} 
		} else { // Look up Name Entity
			Set<Long> neSentenceIds = this.nePhraseNodeDao.getSentenceIdsContainingNameEntity(aboutParameter);
			if(logger.isDebugEnabled()) {
				logger.info("Number of name entity related sentences: " + neSentenceIds.size());
			}
			List<Sentence> sentences = sentenceRepository.getSentenceById(neSentenceIds);
			sentences = sentences.stream().filter(s -> s.getScore() != null).collect(Collectors.toList());
			if(sentences.size() > 0) {
				answer = new ListAnswer(generateTopNAnswers(sentences, MAX_RETURN));
			}
		}
		
		return answer;
	}

	private String getParameter(List<ParsedWord> parsedQuestion, int i) {
		StringBuilder sb = new StringBuilder();
		for(int index = i; index < parsedQuestion.size(); index++) {
			String word = parsedQuestion.get(index).getWord().replace(".", "");
			sb.append(word.toLowerCase().trim());
			sb.append(" ");
		}
		return sb.toString().trim();
	}
	
	private List<String> generateTopNAnswers(List<Sentence> sentences, int n) {
		Collections.sort(sentences, new Comparator<Sentence>() {
			@Override
			// Sort in descending order
			public int compare(Sentence o1, Sentence o2) {
				return o2.getScore().compareTo(o1.getScore());
			}
		});
		
		Set<String> strings = new HashSet<String>();
		List<String> answers = new ArrayList<String>();
		
		for(Sentence sentence : sentences) {
			if(answers.size() > n) {
				break;
			} else {
				String text = sentence.getText();
				if(!strings.contains(text)) {
					answers.add(text);
				}
				strings.add(text);
			}
		}
		
		return answers;
	}
	

	private boolean invalidForm(List<ParsedWord> parsedQuestion) {
		String firstWord = parsedQuestion.get(0).getWord().toLowerCase();
		String secondWord = parsedQuestion.get(1).getWord().toLowerCase();
		String thirdWord = parsedQuestion.get(2).getWord().toLowerCase();
		
		return !firstWord.equals("tell") || !secondWord.equals("me") || !thirdWord.equals("about");
	}

}
