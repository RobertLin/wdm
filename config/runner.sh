#!/bin/bash
#------------------------------------------------------------ #
# script variables
#------------------------------------------------------------ #
INPUT="config/database-1.txt"
SUPPORT="config/supports-1.txt"

#------------------------------------------------------------ #
# script runner
#------------------------------------------------------------ #
java -cp jar/wdm.jar org.wdm.association.main.MsGeneralizedSequentialPatternMain -i ${INPUT} -s ${SUPPORT}
java -cp jar/wdm.jar org.wdm.association.main.MsPrefixSpanMain -i ${INPUT} -s ${SUPPORT}
