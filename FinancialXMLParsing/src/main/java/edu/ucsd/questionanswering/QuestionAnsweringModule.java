package edu.ucsd.questionanswering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class QuestionAnsweringModule implements ApplicationContextAware {
	private static Map<String, String> questionHandlers = new HashMap<String, String>();
	private static Logger logger = LoggerFactory.getLogger(QuestionAnsweringModule.class);
	
	static {
		questionHandlers.put("which", "whichQuestionHandler");
		questionHandlers.put("when", "whenQuestionHandler");
		questionHandlers.put("tell", "tellMeQuestionHandler");
	}
	
	private ApplicationContext applicationContext;
	
	private StanfordCoreNLP pipeline;

	
	public QuestionAnsweringModule() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner"); 
		pipeline = new StanfordCoreNLP(props);
	}
	
	public Answer answer(String question) {
		if(question == null || question.length() == 0) {
			throw new IllegalArgumentException("A question can not be null or an empty string");
		}
		
		List<String> questionTokens = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(question);
		while(tokenizer.hasMoreTokens()) {
			questionTokens.add(tokenizer.nextToken());
		}
		
		String questionType = questionTokens.get(0).toLowerCase();
		if(questionType == null) {
			return new NoAnswer();
		}
		
		// Find Question Type
		QuestionHandler questionHandler = (QuestionHandler)this.applicationContext.getBean(questionHandlers.get(questionType));
				
		Annotation questionDocument = new Annotation(question);
		pipeline.annotate(questionDocument);
		
		List<CoreMap> sentences = questionDocument.get(SentencesAnnotation.class);
		
		if(sentences.size() != 1) {
			throw new IllegalArgumentException("You can only specify one question.");
		}
		
		List<ParsedWord> parsedQuestion = new ArrayList<ParsedWord>();
		
		for(CoreMap sentence: sentences) {
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);
				// this is the LEMMA
				String lemma = token.get(LemmaAnnotation.class);
				parsedQuestion.add(new ParsedWord(word, pos, ne, lemma));
			}
		}
		
		logger.info("You asked: " + question + " and the answer is: ");
		return questionHandler.answerQuestion(parsedQuestion);
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.applicationContext = arg0;
	}
}
