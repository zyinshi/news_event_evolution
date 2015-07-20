package edu.ucsd.xmlparser.elementhandler;

/**
 * An ElementHandler will perform the necessary operations on the Node pertaining to a given Tag/Element 
 * and then signal to the caller whether to CONTINUE (Visiting the Children) or SKIP_CHILDREN (self explanatory)
 * 
 * @author rogertan
 *
 */
public enum ElementHandlingResult {
	CONTINUE, SKIP_CHILDREN;
}
