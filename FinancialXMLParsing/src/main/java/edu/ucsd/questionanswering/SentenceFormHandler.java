package edu.ucsd.questionanswering;

import edu.ucsd.xmlparser.entity.NeTags;
import edu.ucsd.xmlparser.entity.Word;

public interface SentenceFormHandler {
	Answer handleWord(Word word, NeTags tag);
}
