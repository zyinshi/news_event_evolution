package edu.ucsd.xmlparser;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import edu.ucsd.cvalue.CValueDocumentCalculator;
import edu.ucsd.system.SystemApplicationContext;

import java.util.concurrent.*;

public class ParserMain {
	private static Logger logger = LoggerFactory.getLogger(ParserMain.class);
	
	public static void main(String[] args) throws Exception {
		ApplicationContext context = SystemApplicationContext.getApplicationContext();
		// Directory of raw documents (XML format)
		String base = "/home/ysz/news_graph/article/";
		// Name collection by date (depends on time period size)
		String day = args[0];	
		String folder = base + day;
		List<File> files = Files.walk(Paths.get(folder))
                .filter(foundPath -> foundPath.toString().endsWith(".xml"))
                .map(Path::toFile)
                .collect(Collectors.toList());
		System.out.println("Total Number of files: " + files.size());
		
		/*
		 * old version
		 */
//		String[] fileNames = { "test.xml" };
//		List<File> files = new ArrayList<File>();
//		for(String fileName : fileNames) {
//			files.add(new File(ParserMain.class.getClassLoader().getResource(fileName).getFile()));
//		}
		
		FinancialXMLParser parser = FinancialXMLParser.class.cast(context.getBean("financialXMLParser"));
		
		for(File file : files) {
			logger.info("Now processing file: " + file);
			if(parser.parseAndLoad(file, day)==false) {
				System.out.println(file+": Failed to read file!!!");
				continue;
			}


		}

		/*
		 * Calculate cvalue score to find important terms and sentence in whole document
		 * Don't need this part when extract named entities and cvalue terms, ignore this feature to improve speed
		 */
//		logger.info("Calculating Collection Level CValues");
//		CValueDocumentCalculator cValueDocumentCalculator = CValueDocumentCalculator.class.cast(context.getBean("cValueDocumentCalculator"));
//		cValueDocumentCalculator.computeCollectionLevelCValue();
//		
//		logger.info("Performing Sentence Scoring.");
//		SentenceScorer sentenceScorer = SentenceScorer.class.cast(context.getBean("sentenceScorer"));
//		sentenceScorer.scoreSentence();
		
	}

}
