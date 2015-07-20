package edu.ucsd.xmlparser.entity;

public enum PhraseTypes {
	NNP, NNS, VB;
	
	public static boolean isNNP(String posTag) {
		return PhraseTypes.NNP.name().equals(posTag);
	}
	
	public static boolean isVB(String posTag) {
		return PhraseTypes.VB.name().equals(posTag);		
	}

	public static boolean isNNS(String posTag) {
		return PhraseTypes.NNS.name().equals(posTag);
	}
}
