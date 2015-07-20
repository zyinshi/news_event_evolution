package edu.ucsd.questionanswering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.ucsd.system.SystemApplicationContext;

public class QuestionAnsweringMain {
	private static Logger logger = LoggerFactory.getLogger(QuestionAnsweringMain.class);
	
	public static void main(String[] args) {
		ApplicationContext ctx = SystemApplicationContext.getApplicationContext();
		QuestionAnsweringModule qaModule = QuestionAnsweringModule.class.cast(ctx.getBean("questionAnsweringModule"));
		
		logger.info(qaModule.answer("When did Chevron acquire UCSD?").toString());
		//logger.info(qaModule.answer("Which organizations did Disney acquire?").toString());
		//logger.info(qaModule.answer("Which persons did Disney acquire?").toString());
		
		//logger.info(qaModule.answer("Tell me about Walt Disney.").toString());
		logger.info(qaModule.answer("When did Disney acquire Das Vierte?").toString());
		logger.info(qaModule.answer("When did Chevron acquire Atlas Energy?").toString());
		logger.info(qaModule.answer("When did Disney sell Miramax?").toString());
		logger.info(qaModule.answer("Which organizations did Disney sell?").toString());
		logger.info(qaModule.answer("Tell me about The Secret to Life.").toString());
		logger.info(qaModule.answer("Which organizations did Chevron acquire?").toString());
		
		logger.info(qaModule.answer("Tell me about Chevron.").toString());
		logger.info(qaModule.answer("Tell me about operating income.").toString());
		// Questions that we can answer
		
		/* logger.info(qaModule.answer("Which organizations did Disney acquire?").toString());
		logger.info(qaModule.answer("Which organizations did Walt Disney acquire?").toString());
		logger.info(qaModule.answer("Which organizations did Chevron acquire?").toString());
		logger.info(qaModule.answer("When did Chevron acquire Atlas Energy?").toString());
		logger.info(qaModule.answer("Which organizations did Disney sell?").toString());
		logger.info(qaModule.answer("When did Disney acquire Das Vierte?").toString());
		logger.info(qaModule.answer("Tell me about segment operating income.").toString());
		*/
		// Cannot answer
		/*
		// Completed the acquisition
		logger.info(qaModule.answer("When did Disney acquire Playdom, Inc?").toString()); // Info is there but incorrect tagging
		logger.info(qaModule.answer("When did Disney acquire Lucasfilm?").toString());
		logger.info(qaModule.answer("Which organizations did Walt Sprint acquire?").toString());
		*/
		/*
		qaModule.answer("What acquisitions did Chevron make?"); // Unable to answer this one
		*/
		
	}
}
