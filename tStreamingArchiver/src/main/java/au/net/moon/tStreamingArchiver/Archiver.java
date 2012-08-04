package au.net.moon.tStreamingArchiver;
/**
 * tStreamingArchiver - get tweets using the Twitter StreamAPI
 * Copyright (C) 2012 Brenda Moon 
 * 
 * This program is free software; you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 **/

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import au.net.moon.tUtils.RedirectSystemLogs;
import au.net.moon.tUtils.SearchFilter;
import au.net.moon.tUtils.SimpleSSLMail;
import au.net.moon.tUtils.twitterAuthorise;

/**
 * Use the Twitter StreamingAPI to get filtered tweets and track some peoples
 * tweets. StreamingAPI is case insensitive so keywords only need to be
 * lowercase. Keywords are stored in searches.txt file in ./data directory
 * <p>
 * 
 * Twitter StreamingAPI now seems to support multi word search terms - they are
 * treated as "and" separated, not as phrases. Might want to review my search
 * terms and see if any should be re-joined.
 * <p>
 * 
 * Needs two config files to exist in program directory or root of classpath
 * directory: twitter4j.properties with Twitter oath parameters set and
 * tArchiver.properties with email parameters set (this also contains the SQL
 * parameters for the modules that need them)
 * <p>
 * 
 * See twitter4j.properties.sample and tArchiver.properties.sample in
 * distribution.
 * <p>
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * @version 1.00
 * 
 */
// I've considered using serialisation instead of "toString()" for saving the
// item to disk. The status & deletion notice are serialisable. But the Track
// Limitation & GeoScrub are not.
// From what I've read, serialisation is likely to be worse performance?
public class Archiver implements StatusListener {
	static TwitterStream twitterStream;
	public SaveToDisk std;
	static int waitSeconds;
	static SimpleSSLMail sendMail;
	static Boolean debug;
	SimpleDateFormat myFormatter = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss Z");

	/**
	 * Setup log files and then open a Twitter streaming api connection to
	 * collect data.
	 * 
	 * @param args
	 *            none
	 * @throws TwitterException
	 */
	public static void main(String[] args) throws TwitterException {
		debug = false;
		if (!debug) {
			new RedirectSystemLogs("TwitterArchiverLog.%g.log");
		} else {
			System.out
					.println("TwitterStreamingArchiver: Logging to system log files");
		}

		System.out
				.println("TwitterStreamingArchiver: Program Starting... (v1.00)");
		
		SearchFilter searchFilter = new SearchFilter();
		Archiver archiver = new Archiver();
		// twitterStream.addStatusListener(archiver);
		twitterStream.addListener(archiver);
		// sample() == gardenhose
		// twitterStream.sample();
		// seems like historyCount isn't supported yet?
		final int historyCount = 0;
		FilterQuery query = new FilterQuery();
		query.follow(searchFilter.followArray());
		query.track(searchFilter.trackArray());
		query.count(historyCount);
		twitterStream.filter(query);
		// TODO: work out how to update the filter when it has changed
		// searchFilter.update() checks and returns true if searches have
		// changed. See my notes about how to do it manually.
	}

	Archiver() {
		// Set timezone to UTC for the Twitter created at dates
		myFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		twitterAuthorise twitterAuth = new twitterAuthorise(true);
		twitterStream = twitterAuth.getTwitterStream();
		std = new SaveToDisk();
		waitSeconds = 0;
		sendMail = new SimpleSSLMail();
	}

	/*
	 * (non-Javadoc) When a tweet is received, save it to disk.
	 * 
	 * @see twitter4j.StatusListener#onStatus(twitter4j.Status)
	 */
	public void onStatus(Status status) {
		waitSeconds = 0;
		std.save(status.toString());
	}

	/*
	 * (non-Javadoc) When a deletion notice is received, save it to disk.
	 * 
	 * @see
	 * twitter4j.StatusListener#onDeletionNotice(twitter4j.StatusDeletionNotice)
	 */
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		waitSeconds = 0;
		System.out.println("DeletionNotice:" + statusDeletionNotice.toString());
		// save the deletion notice to the archive files
		std.save(statusDeletionNotice.toString());
	}

	/*
	 * (non-Javadoc) When a rate limitiation notice is received, save it to
	 * disk.
	 * 
	 * @see twitter4j.StatusListener#onTrackLimitationNotice(int)
	 */
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		waitSeconds = 0;
		String message = "TrackLimitationNotice: number of statuses skipped by rate limiting="
				+ numberOfLimitedStatuses;
		System.out.println(message);
		std.save(message);
		sendMail.sendMessage(
				"TwitterStreamingArchiver: TrackLimitationNotice ",
				"TwitterStreamingArchiver: " + message);
	}

	/*
	 * (non-Javadoc) Handle Twitter exceptions.
	 * 
	 * @see twitter4j.StreamListener#onException(java.lang.Exception)
	 */
	public void onException(Exception ex) {
		String errorMessageText = "";
		boolean errorExit = true;
		System.out.println("Error: " + ex.toString());
		if (ex.toString().contains("TwitterException")) {
			TwitterException tex = (TwitterException) ex;
			if (tex.getStatusCode() == 401) {
				errorMessageText = "ERROR: Invalid userid or password for Twitter";
			} else if (tex.getStatusCode() == 404) {
				errorMessageText = "ERROR: Parameter not allowed for resource";
			} else if (tex.getStatusCode() == 406) {
				errorMessageText = "ERROR: Parameter not allowed for resource";
			} else if (tex.getStatusCode() == 413) {
				errorMessageText = "ERROR: Parameter too long";
			} else if (tex.getStatusCode() == 416) {
				errorMessageText = "ERROR: Parameter range unacceptable";
			} else if (tex.getStatusCode() == 500) {
				errorMessageText = "ERROR: Server Internal Error - contact Twitter API team";
				errorExit = false;
			} else if (tex.getStatusCode() == 503) {
				System.err
						.println("ERROR: Service Overloaded - contact Twitter API team");
				errorExit = false;
			} else if (tex.getStatusCode() == -1) {
				errorMessageText = "ERROR: status code -1 - maybe a dropped stream?";
				errorExit = false;
			}
			// System.out.println("rateLimit: " + tex.getRateLimitStatus());
			if (!errorExit) {
				System.err.println(errorMessageText);
				sendMail.sendMessage(
						"TwitterStreamingArchiver: Twitter Exception",
						errorMessageText);
			} else {
				// give up
				System.err.println(errorMessageText);
				ex.printStackTrace();
				sendMail.sendMessage(
						"TwitterStreamingArchiver: Twitter Exception",
						"Error Exit:\n" + errorMessageText + "\n"
								+ ex.toString());
				System.exit(-1);
			}
		} else {
			ex.printStackTrace();
			sendMail.sendMessage("TwitterStreamingArchiver: Other Error",
					"Other Error:\n" + ex.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see twitter4j.StatusListener#onScrubGeo(long, long)
	 */
	public void onScrubGeo(long userId, long upToStatusId) {
		// This is a request to remove Geo information from all tweets for that
		// userId
		// up to the given upToStatusId
		// TODO: implement onScrubGeo
		String message = "ScrubGeoNotice: userId=" + userId + " upToStatusId="
				+ upToStatusId;
		System.out.println(message);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		System.out.println("Got stall warning:" + warning);
	}
}
