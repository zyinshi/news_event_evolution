package edu.ucsd.cvalue;

import java.util.List;

public class CValueCalculator {

	/**
	 * CValue data ordered according to their absolute frequency
	 * in the corpus
	 * 
	 * @param cValueData
	 */
	public static void calculate(List<CValueData> cValueData) {
		if(cValueData == null || cValueData.size() == 0) {
			return;
		}
		
		int maxLength = cValueData.get(0).getPhraseLength();
		
		for(int i = 0; i < cValueData.size(); i++) {
			computeCValue(maxLength, i, cValueData);
		}
	}
	
	private static void computeCValue(int maxLength, int i,
			List<CValueData> cValueData) {
		CValueData cValue = cValueData.get(i);
		int phraseLength = cValue.getPhraseLength();
		// For all phrases of maximum length, we use a simpler formula
		// discounting its presence in other strings since those strings
		// will not be contained in other strings by definition
		if(phraseLength == maxLength) {
			cValue.setCValue(maxLengthCValue(phraseLength, cValue.getPhraseFrequency()));
		} else {
			for(int j = 0; j < i; j++) {
				CValueData longerPhrase = cValueData.get(j);
				longerPhrase.computeContainment(cValue);
			}
			if(cValue.getNumberOfSentencesContainedIn() != 0) {
				cValue.setCValue(smallerStringsCValue(cValue.getPhraseLength(), cValue.getPhraseFrequency(), cValue.totalNumberOfTimesContainedInLongerPhrase(), cValue.getNumberOfSentencesContainedIn()));
			} else {
				cValue.setCValue(maxLengthCValue(cValue.getPhraseLength(), cValue.getPhraseFrequency()));
			}
		}
	}

	private static double smallerStringsCValue(int phraseLength, int phraseFrequency, 
			int numberOfTimesPhraseAppearsInLongerPhrase, int numberOfLongerPhrases) {
		System.out.println("Phrase Length: " + phraseLength);
		System.out.println("Phrase Frequency: " + phraseFrequency);
		System.out.println("Number of Times Phrase Appears in Longer Phrase: " + numberOfTimesPhraseAppearsInLongerPhrase);
		System.out.println("Number of longer Phrases: " + numberOfLongerPhrases);
		return MathUtils.log2(phraseLength) * (phraseFrequency - (numberOfTimesPhraseAppearsInLongerPhrase/numberOfLongerPhrases));
	}
	
	private static double maxLengthCValue(int phraseLength, int phraseFrequency) {
		return phraseFrequency * MathUtils.log2(phraseLength);
	}

}
