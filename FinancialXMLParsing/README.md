FinancialXMLParsing
===================

Building the Project
====================

1. This project utilizes a number of libraries that can't be found in public repositories for example:
   a) Simple NLG https://code.google.com/p/simplenlg/. You will need to download the zip file for version 4.4, unzip it in a local directory and then use the
      provided maven-local-install script to install the jars in the local maven repository. Note that you'll probably need to adjust the file path (currently)
      the script assumes that the jars will be in the same directory.

      Library Usage: generate past tense of a word      

   b) YAWNI (Yet Another Word Net Library). Follow instructions here: http://www.yawni.org/wiki/Main/Developers. And yes, you'll need to download the codebase
      from GitHub i.e. having the Maven dependencies is not enough. 

      Library Usage: API to access Word Net particularly version 3.0. This is currently used within the project to generate Nouns from Verbs amont other things
