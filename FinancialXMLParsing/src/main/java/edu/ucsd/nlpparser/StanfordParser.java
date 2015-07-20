package edu.ucsd.nlpparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.neo4j.graphalgo.impl.ancestor.AncestorsUtil;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.kernel.Traversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.tokensregex.MultiPatternMatcher;
import edu.stanford.nlp.ling.tokensregex.SequenceMatchResult;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.ucsd.cvalue.CValueRawFrequency;
import edu.ucsd.xmlparser.dao.SentenceDao;
import edu.ucsd.xmlparser.entity.ApplicationRelationshipType;
import edu.ucsd.xmlparser.entity.Sentence;
import edu.ucsd.xmlparser.entity.SpecialTokens;
import edu.ucsd.xmlparser.entity.Word;
import edu.ucsd.xmlparser.entity.WordToWordDependency;

@SuppressWarnings("deprecation")
public class StanfordParser {
	private static Logger logger = LoggerFactory.getLogger(StanfordParser.class);
	
	private SentenceDao sentenceDao;
	private Neo4jTemplate template;
	
	private StanfordCoreNLP pipeline;
	
	private MultiPatternMatcher<CoreMap> multiPatternMatcher;
	
	@Inject
	public void setSentenceDao(SentenceDao sentenceDao) {
		this.sentenceDao = sentenceDao;
	}
	
	@Inject
	public void setTemplate(Neo4jTemplate template) {
		this.template = template;
	}
	
	public void initiate() {
		Properties props = new Properties();
		props.put("annotators", "tokenize, sspl"
				+ "it, pos, lemma, ner, parse"); 
		pipeline = new StanfordCoreNLP(props);
		
		List<TokenSequencePattern> patterns = new ArrayList<TokenSequencePattern>();
		patterns.add(TokenSequencePattern.compile("[{tag: NN}]+[tag: NN]"));
		patterns.add(TokenSequencePattern.compile("[{tag: /JJ|NN/}]+[tag: NN]"));
		patterns.add(TokenSequencePattern.compile("( ([{tag: /JJ|NN/}]+) | ( ([{tag: /JJ|NN/}]*)([{tag: IN}]?) )([{tag: /JJ|NN/}]*) )[tag:NN]"));
		 
		multiPatternMatcher = TokenSequencePattern.getMultiPatternMatcher(patterns);
	}
	
	/**
	 * For every paragraph, there may be multiple sentences, which is why we return a list
	 * 
	 * @param text
	 * @param noSentence
	 * @param sectionId 
	 * @param sectionTermAndFrequency 
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public List<Sentence> parseAndLoad(String text, int noSentence, Map<String, CValueRawFrequency> termAndFrequency, Long documentId, Long sectionId, Map<String, CValueRawFrequency> sectionTermAndFrequency) {
		if(text == null) {
			throw new IllegalArgumentException("Sentence can not be null");
		}
		
		List<Sentence> result = new ArrayList<Sentence>();
		
		Sentence newSentence = null;

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		long startTime = System.currentTimeMillis();
		pipeline.annotate(document);
		long endTime = System.currentTimeMillis();
		
		logger.info("Stanford Parsing takes : " + (endTime - startTime) + " ms");

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for(CoreMap sentence: sentences) {
			int wordIndex = 0; 

			// Maintain a list of words that has already been seen
			Map<Word.TextAndPosition, Word> seenWords = new HashMap<Word.TextAndPosition, Word>();
			Word root = Word.newWord(SpecialTokens.ROOT.name(), wordIndex);
			wordIndex++;

			sentenceDao.save(root);

			Node prevNode = template.getNode(root.getId());

			seenWords.put(root.getTextAndPosition(), root);

			newSentence = Sentence.newSentence(sentence.get(TextAnnotation.class), noSentence);

			newSentence.addWord(root);
			
			for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				Word newWord = Word.newWord(word, wordIndex);
				newWord.setNameEntityTag(ne);
				newWord.setPosTag(pos);
				sentenceDao.save(newWord);

				Node currNode = template.getNode(newWord.getId());
				template.createRelationshipBetween(prevNode, currNode, ApplicationRelationshipType.NEXT_WORD.name(), new HashMap<String, Object>());

				prevNode = currNode;

				seenWords.put(newWord.getTextAndPosition(), newWord);

				newSentence.addWord(newWord);

				wordIndex++;
			}

			sentenceDao.save(newSentence);

			List<SequenceMatchResult<CoreMap>> results = multiPatternMatcher.findNonOverlapping(sentence.get(TokensAnnotation.class));
			for(SequenceMatchResult<CoreMap> res : results) {
				String term = res.group().toLowerCase();
				CValueRawFrequency frequency = termAndFrequency.get(term);					
				if(frequency == null) {
					frequency = new CValueRawFrequency();
					termAndFrequency.put(term, frequency);
				} 
				frequency.addSentenceId(newSentence.getId());
				frequency.incrementFrequency();
				
				frequency = sectionTermAndFrequency.get(term);					
				if(frequency == null) {
					frequency = new CValueRawFrequency();
					sectionTermAndFrequency.put(term, frequency);
				} 
				frequency.addSentenceId(newSentence.getId());
				frequency.incrementFrequency();
			}

			
			Node sentenceNode = template.getNode(newSentence.getId());
			template.createRelationshipBetween(sentenceNode, template.getNode(root.getId()), 
					ApplicationRelationshipType.FIRST_WORD.name(), new HashMap<String, Object>());

			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			// Get dependency tree
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			Collection<TypedDependency> tds = gs.typedDependenciesCollapsed();
			for(TypedDependency td : tds) {
				// Check for existence of words
				Word startWord = getWord(td.gov(), seenWords);
				Word endWord = getWord(td.dep(), seenWords);

				WordToWordDependency dependency = new WordToWordDependency(startWord, endWord, td.reln().toString());
				sentenceDao.save(dependency);
			}

			// Now save the parse trees as well
			StanfordParseTreeSaver spts = new StanfordParseTreeSaver(sentenceDao, template, newSentence, seenWords);
			spts.performDepthFirstTraversal(tree, sectionId, documentId);

			seenWords.clear();
			
			result.add(newSentence);
		}

		// Process coreference
		Map<Integer, CorefChain> coref = document.get(CorefChainAnnotation.class);
		if(coref != null) {
			for(Map.Entry<Integer, CorefChain> entry : coref.entrySet()) {
				CorefChain c = entry.getValue();
				//this is because it prints out a lot of self references which aren't that useful

				if(c.getMentionsInTextualOrder().size() <= 1)
					continue;

				CorefMention cm = c.getRepresentativeMention();

				String clust = "";
				List<CoreLabel> tks = document.get(SentencesAnnotation.class).get(cm.sentNum-1).get(TokensAnnotation.class);
				if(logger.isDebugEnabled()) {
					logger.debug("Start Index: " + cm.startIndex + " End Index: " + cm.endIndex);
				}

				for(int i = cm.startIndex-1; i < cm.endIndex-1; i++) {
					clust += tks.get(i).get(TextAnnotation.class) + " ";
				}
				clust = clust.trim();

				if(logger.isDebugEnabled()) {
					logger.debug("representative mention: \"" + clust + "\" is mentioned by:");
				}

				logger.info("Representative mention: \"" + clust + "\" in sentence number: " + (cm.sentNum-1) + " is mentioned by:");

				Node representativeNode = getRepresentativeNode(cm.sentNum-1, cm.startIndex, cm.endIndex);
				if(logger.isDebugEnabled()) {
					if(representativeNode != null) {
						if(representativeNode.hasProperty("value")) {
							logger.debug("Rep Node Value : " + representativeNode.getProperty("value"));
						} else {
							logger.debug("Rep Node Text : " + representativeNode.getProperty("text"));
						}
					} else {
						logger.debug("Repr Node is somehow null.");
					}
				}
				
				for(CorefMention m : c.getMentionsInTextualOrder()) {
					String clust2 = "";
					tks = document.get(SentencesAnnotation.class).get(m.sentNum-1).get(TokensAnnotation.class);
					for(int i = m.startIndex-1; i < m.endIndex-1; i++)
						clust2 += tks.get(i).get(TextAnnotation.class) + " ";
					clust2 = clust2.trim();
					//don't need the self mention

					if(clust.equals(clust2))
						continue;

					// Not a self reference
					Node mention = getRepresentativeNode(m.sentNum-1, m.startIndex, m.endIndex);
					if(mention != null) {
						if(logger.isDebugEnabled()) {
							if(mention.hasProperty("text")) {
								logger.debug("Mention : " + mention.getProperty("text"));
							} else {
								logger.debug("Mention : " + mention.getProperty("value"));
							}
						}
						template.createRelationshipBetween(mention, representativeNode, ApplicationRelationshipType.REFERS_TO.name(), new HashMap<String, Object>());
					}
					logger.info("\t" + clust2 + " in sentence number: " + (m.sentNum-1));
				}

			}
		} // if (coreref is not null)
		
		return result;
	}
	
	private Node getRepresentativeNode(int sentenceNumber, int startIndex, int endIndex) {
		if(endIndex != (startIndex + 1)) {
			List<Node> reprMention = sentenceDao.getWordsFromTo(sentenceNumber, startIndex, endIndex);
		
			if(logger.isDebugEnabled()) {
				for(Node word: reprMention) {
					logger.info("Word Position: " + word.getProperty("position") + " Word text: " + word.getProperty("text"));
				}
			}
		
			return AncestorsUtil.lowestCommonAncestor(reprMention, Traversal.expanderForTypes(ApplicationRelationshipType.HAS_PARSE_CHILD, Direction.INCOMING));
		}
		
		return sentenceDao.getWord(sentenceNumber, startIndex);
	}
	
	private Word getWord(TreeGraphNode node, Map<Word.TextAndPosition, Word> seenWords) {
		Word word = Word.newWord(node.nodeString(), node.label().get(IndexAnnotation.class));
		return seenWords.get(word.getTextAndPosition());
	}

}
