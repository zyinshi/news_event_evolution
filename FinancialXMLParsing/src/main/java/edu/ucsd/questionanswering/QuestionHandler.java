package edu.ucsd.questionanswering;

import java.util.List;

public interface QuestionHandler {
	Answer answerQuestion(List<ParsedWord> parsedQuestion);
}
