package edu.ucsd.wordnet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.atteo.evo.inflector.English;
import org.yawni.wordnet.POS;
import org.yawni.wordnet.Relation;
import org.yawni.wordnet.WordNet;
import org.yawni.wordnet.WordSense;

import simplenlg.features.Feature;
import simplenlg.features.Tense;
import simplenlg.framework.InflectedWordElement;
import simplenlg.framework.LexicalCategory;
import simplenlg.framework.WordElement;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

/**
 * The LexicalUtility class pertains to generating various forms of words and we currently use a WordNet 
 * utility library to generate past tenses and noun forms of Verbs. 
 * 
 * @author rogertan
 *
 */
public class LexicalUtility {
	private static WordNet wordNet = WordNet.getInstance();
	private static Lexicon lexicon;
	private static Realiser realiser;
	
	static {
		lexicon = Lexicon.getDefaultLexicon();
        realiser = new Realiser(lexicon);
	}
	
	public static Set<String> getNounsIncludingPluralFormsForVerb(String verbWord) {
		List<WordSense> senses = wordNet.lookupWordSenses(verbWord, POS.VERB);
		Set<String> nounForms = new HashSet<String>();
		for(WordSense sense : senses) {
			for(Relation relation : sense.getRelations()) {
				if(relation.getTarget().getPOS().equals(POS.NOUN)) {
					String noun = relation.getTarget().getDescription();
					nounForms.add(noun);
					nounForms.add(English.plural(noun));
					break;
				}
			}
		}
		return nounForms;
	}
	
	public static String getPastTense(String verbWord) {
		WordElement word = lexicon.getWord(verbWord, LexicalCategory.VERB);
		InflectedWordElement infl = new InflectedWordElement(word);
		infl.setFeature(Feature.TENSE, Tense.PAST);
		
		return realiser.realise(infl).getRealisation();
	}
}
