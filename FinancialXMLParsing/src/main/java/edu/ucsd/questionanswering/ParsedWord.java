package edu.ucsd.questionanswering;

public class ParsedWord {
	private String word;
	private String posTag;
	private String neTag;
	private String lemma;
	
	public ParsedWord(String word, String posTag, String neTag, String lemma) {
		super();
		this.word = word;
		this.posTag = posTag;
		this.neTag = neTag;
		this.lemma = lemma;
	}

	public String getWord() {
		return word;
	}

	public String getPosTag() {
		return posTag;
	}

	public String getNeTag() {
		return neTag;
	}

	public String getLemma() {
		return lemma;
	}

	public boolean isLikelyPresentTense() {
		return getWord().toLowerCase().equals(getLemma().toLowerCase());
	}
}
