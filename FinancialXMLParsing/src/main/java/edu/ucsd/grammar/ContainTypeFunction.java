package edu.ucsd.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ContainTypeFunction implements TypeFunction {
	private final static Set<VariableTypes> validWordContains = new HashSet<VariableTypes>();
	private final static Set<VariableTypes> validSentenceContains = new HashSet<VariableTypes>();
	private final static Set<VariableTypes> validDocumentContains = new HashSet<VariableTypes>();
		
	private Map<VariableTypes, Set<VariableTypes>> typeToValidContainedTypes = new HashMap<VariableTypes, Set<VariableTypes>>();

	static {
		validSentenceContains.add(VariableTypes.WORD);
		validSentenceContains.add(VariableTypes.PHRASE);
		validDocumentContains.add(VariableTypes.WORD);
		validDocumentContains.add(VariableTypes.PHRASE);
		validDocumentContains.add(VariableTypes.SENTENCE);
	}
	
	public ContainTypeFunction() {
		typeToValidContainedTypes.put(VariableTypes.WORD, validWordContains);
		typeToValidContainedTypes.put(VariableTypes.SENTENCE, validSentenceContains);
		typeToValidContainedTypes.put(VariableTypes.DOCUMENT, validDocumentContains);		
	}
	
	@Override
	public boolean isValid(VariableTypes callee, VariableTypes parameter) {
		boolean isValid = false;
		
		Set<VariableTypes> validContainedTypes = this.typeToValidContainedTypes.get(callee);
		if(validContainedTypes != null) {
			if(validContainedTypes.contains(parameter)) {
				isValid = true;
			}
		}
		
		return isValid;
	}
	
}
