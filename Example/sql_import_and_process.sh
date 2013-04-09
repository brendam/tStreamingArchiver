#!/bin/bash
# using check_errs() to make sure that the script doesn't keep going after an error in one of the java programs
check_errs()
{
  # from: http://steve-parker.org/sh/exitcodes.shtml
  # I added sending an email with the error
  # Function. Parameter 1 is the return code
  # Para. 2 is text to display on failure.
  if [ "${1}" -ne "0" ]; then
    echo "ERROR # ${1} : ${2}"
    # exit with the right error code.
	SUBJECT="AtlasOfNow: SQL Import and Process"
## make sure you set your email here ######################
	EMAIL="you@email.goes.here"
	# Email text/message
	EMAILMESSAGE="/tmp/emailmessage.txt"
    echo "ERROR # ${1} : ${2}" > $EMAILMESSAGE
    # echo "second line of message" >> $EMAILMESSAGE
	# send an email using /bin/mail
	/usr/bin/mail -s "$SUBJECT" "$EMAIL" < $EMAILMESSAGE
    exit ${1}
  fi
}

# cd to full path to your archiver directory (set and uncomment the next line for use from CRON)
# cd /fullPathToArchiverDirectory/Example/

if [ ! -e tDiskToSQL.0.log.lck ]		# If lck file exist then already running! 
	then
        java -Xmx2048m -jar tDiskToSQL-0.0.2-SNAPSHOT-jar-with-dependencies.jar
        check_errs $? "tDiskToSQL returned an error, see logfile (tDiskToSQL.0.log) for details"
		if [ ! -e SearchImport.0.lck ]		# If lck file exist then already running! 
			then
		       java -jar tSearchImport-0.0.1-SNAPSHOT-jar-with-dependencies.jar
		       check_errs $? "tSearchImport returned an error, see logfile (SearchImport.0.log) for details"
			   if [ ! -e UpdateSearchTermIndexing.0.lck ]		# If lck file exist then already running! 
			       then
			       java -jar tUpdateSearchTermIndexing-0.0.1-SNAPSHOT-jar-with-dependencies.jar
			       check_errs $? "UpdateSearchTermIndexing returned an error, see logfile (UpdateSearchTermIndexing.0.log) for details"
#					if [ ! -e BuildWordLists.0.lck ]		# If lck file exist then already running! 
#						then
#					       java -jar tBuildWordLists-0.0.1-SNAPSHOT-jar-with-dependencies.jar
#					       check_errs $? "BuildWordLists returned an error, see logfile (BuildWordLists.0.log) for details"
#					fi
			   fi
   	    fi
fi
