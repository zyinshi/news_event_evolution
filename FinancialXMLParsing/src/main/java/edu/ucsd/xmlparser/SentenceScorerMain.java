package edu.ucsd.xmlparser;

import org.springframework.context.ApplicationContext;

import edu.ucsd.system.SystemApplicationContext;

public class SentenceScorerMain {

	public static void main(String[] args) {
		ApplicationContext context = SystemApplicationContext.getApplicationContext();
		SentenceScorer sentenceScorer = SentenceScorer.class.cast(context.getBean("sentenceScorer"));
		sentenceScorer.scoreSentence();
	}

}
