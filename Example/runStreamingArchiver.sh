#!/bin/bash
# cd to full path to your archiver directory (set and uncomment the next line for use from CRON)
# cd /fullPathToArchiverDirectory/Example/
FILTER=$(find . -type f \( -name "TwitterArchiverLog*.lck" \) )

if [ -z ${FILTER} ]; then       
	java -jar tStreamingArchiver-0.0.1-SNAPSHOT-jar-with-dependencies.jar
fi
