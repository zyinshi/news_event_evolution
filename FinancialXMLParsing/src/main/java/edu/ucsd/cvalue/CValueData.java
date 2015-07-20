package edu.ucsd.cvalue;

public class CValueData {
	private String phrase;
	private int phraseFrequency;
	private int phraseLength;
	private int adjustedPhraseFrequency;
	private int numberOfSentencesContainedIn = 0;
	
	private Double cValue = 0.0;
	// We may cache certain statistics about the substrings 
	
	public CValueData(String phrase, int initialPhraseFrequency,
			int phraseLength) {
		this.phrase = phrase;
		this.phraseFrequency = initialPhraseFrequency;
		this.adjustedPhraseFrequency = initialPhraseFrequency;
		this.phraseLength = phraseLength;
	}

	public String getPhrase() {
		return phrase;
	}

	public int getPhraseFrequency() {
		return phraseFrequency;
	}

	public int getPhraseLength() {
		return phraseLength;
	}

	public Double getCValue() {
		return cValue;
	}

	public void setCValue(double cValue) {
		this.cValue = cValue;
	}
	
	public int getAdjustedPhraseFrequency() {
		return this.adjustedPhraseFrequency;
	}
	
	public int getNumberOfSentencesContainedIn() {
		return this.numberOfSentencesContainedIn;
	}
	
	public void computeContainment(CValueData cValue) {
		if(phrase.contains(cValue.getPhrase())) {
			cValue.adjustedPhraseFrequency = cValue.adjustedPhraseFrequency - this.adjustedPhraseFrequency;
			cValue.incrementContainment();
		}
	}
	
	public int totalNumberOfTimesContainedInLongerPhrase() {
		return this.phraseFrequency - this.adjustedPhraseFrequency;
	}

	private void incrementContainment() {
		numberOfSentencesContainedIn++;
	}
}
