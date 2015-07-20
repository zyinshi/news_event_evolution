JFLAGS=-d
JCC=javacc
JC=javac
J=java

all: clean-java clean copy-interfaces generate compile

compile:
	mkdir -p classes
	$(JC) $(JFLAGS) classes *.java

copy-interfaces:
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ContainTypeFunction.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ContainsConstraint.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ForClause.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ForClauseType.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/VariableAssignment.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/VariableDeclaration.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ParsedQuery.java . 
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ValidationException.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/VariableTypes.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/WhereClause.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/WhereClauseType.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/WordConstraint.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/ReturnClause.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/TypeFunctions.java .
	cp ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/TypeFunction.java .

copy-java-files:
	cp *.java ../FinancialXMLParser/src/main/java/edu/ucsd/grammar/

default: compile

generate:
	$(JCC) extractor.jj	

run:
	$(J) -classpath ./classes edu.ucsd.QueryParser < input.txt 

run-error:
	$(J) -classpath ./classes edu.ucsd.QueryParser < error-input.txt 


clean-java:
	rm -f *.java

clean:
	rm -rf classes/**
