package edu.ucsd.xmlparser.elementhandler;

import org.w3c.dom.Node;

public abstract class AbstractElementHandler {
	private DocumentNumberFountain numberFountain;
	
	public final void setNumberFountain(DocumentNumberFountain numberFountain) {
		if(this.numberFountain == null) {
			this.numberFountain = numberFountain;
		}
	}
	
	protected DocumentNumberFountain getNumberFountain() {
		return this.numberFountain;
	}
	
	public ElementHandlingResult handleNodeElement(Node node) {
		return null;
	}
}
