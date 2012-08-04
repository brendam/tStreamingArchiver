package au.net.moon.tUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Redirect the system logs to local rolling log files.
 * 
 * @see http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
 */
public class RedirectSystemLogs {

	public RedirectSystemLogs(String logFileName) {
		// Start Logging Code **********************************************
		// see
		// http://blogs.sun.com/nickstephen/entry/java_redirecting_system_out_and
		//
		// initialize logging to go to rolling log file
		LogManager logManager = LogManager.getLogManager();
		logManager.reset();
		// log file max size 10K, 3 rolling files, append-on-open
		Handler fileHandler = null;
		try {
			fileHandler = new FileHandler(logFileName, 30000, 30, true);
		} catch (SecurityException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		fileHandler.setFormatter(new SimpleFormatter());
		Logger.getLogger("").addHandler(fileHandler);
		// preserve old stdout/stderr streams in case they might be useful
		// PrintStream stdout = System.out;
		// PrintStream stderr = System.err;
		// now rebind stdout/stderr to logger
		Logger logger;
		LoggingOutputStream los;
		logger = Logger.getLogger("stdout");
		los = new LoggingOutputStream(logger, StdOutErrLevel.STDOUT);
		System.setOut(new PrintStream(los, true));
		logger = Logger.getLogger("stderr");
		los = new LoggingOutputStream(logger, StdOutErrLevel.STDERR);
		System.setErr(new PrintStream(los, true));
		// End Logging Code
		// *************************************************
	}
}