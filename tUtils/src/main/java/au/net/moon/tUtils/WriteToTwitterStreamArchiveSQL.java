package au.net.moon.tUtils;
/**
 * WriteToTwitterStreamArchiveSQL - Write Twitter data into the TwitterStreamArchive mySQL database.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

import au.net.moon.tUtils.twitterFields;

/**
 * Write Twitter data into the TwitterStreamArchive mySQL database. Accept data
 * from three sources - Streaming API, TwapperKeeper archive and the Twitter
 * Search API. Honours Twitter deletion notices by not storing 'deleted' tweets
 * and removing them from the database if already stored.
 * <p>
 * 
 * Needs two config files to exist in program directory or 
 * root of classpath directory:
 * twitter4j.properties with Twitter oath parameters set
 * and
 * tArchiver.properties SQL parameters (and email parameters used for error emails)
 * <p>
 * 
 * See twitter4j.properties.sample and tArchiver.properties.sample in distribution.
 * <p>
 */
public class WriteToTwitterStreamArchiveSQL {
	static Statement stmt;
	static ResultSet rs;
	static HashMap<String, String> deletionNotices;
	static Date lastRecordDate;
	static String lastRecordTweetId;
	Boolean dataBaseOk;
	String theAPI;

	/**
	 * @param sourceAPI
	 *            the source of the data to be stored
	 */
	public WriteToTwitterStreamArchiveSQL(String sourceAPI) {
		if (sourceAPI.equals("Stream") || sourceAPI.equals("TwapperKeeper")
				|| sourceAPI.equals("Search") || sourceAPI.equals("Status")) {
			theAPI = sourceAPI;
			openSQLDataBase();
			loadDeletionNotices();
		} else {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Terminating program - unexpected API type: "
					+ sourceAPI);
			System.exit(-1);
		}
	}

	/**
	 * Open the twitter_stream_archive mySQL database
	 */
	private void openSQLDataBase() {
		dataBaseOk = true;
		String database = null;
		String dbUser = null;
		String dbPass = null;

		// SQL variables
		Properties prop = new Properties();
		try {
			//load a properties file
			prop.load(new FileInputStream("tArchiver.properties"));
			// Email account for error messages
			database = prop.getProperty("database");
			dbUser = prop.getProperty("dbUser");
			dbPass = prop.getProperty("dbPassword");
		} catch (IOException ex) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: failed to load database properties from tArchiver.properties");
			ex.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}

		// Register the JDBC driver for MySQL.
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: SQL driver not found!");
			e1.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
		stmt = null;
		rs = null;
		Connection con = null;
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
					+ database, dbUser, dbPass);
		} catch (Exception e) {
			e.printStackTrace();
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Database connection failed! db: "
					+ database);
			dataBaseOk = false;
			System.exit(-1);
		}
		try {
			stmt = (Statement) con.createStatement();
		} catch (SQLException e1) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: failed to create an empty SQL statement");
			e1.printStackTrace();
			dataBaseOk = false;
			System.exit(-1);
		}
	}

	/**
	 * Check if database is ready to use.
	 * 
	 * @return <CODE>true</CODE> if the database is open and ready to use,
	 *         <CODE>false</CODE> otherwise
	 */
	public Boolean isDatabaseReady() {
		return dataBaseOk;
	}

	/**
	 * Load existing deletion notices from the database into the deletionNotices
	 * class field.
	 */
	private void loadDeletionNotices() {
		// `id`, `userId`,`isDeleted`
		// Don't think we need the userId ??
		deletionNotices = new HashMap<String, String>();
		try {
			// As number of notices increases, we might want to filter on 'where
			// isDeleted = false' but then might not delete it if it occurs in a
			// different data source.
			rs = stmt.executeQuery("select * from dNotices");
			while (rs.next()) {
				deletionNotices.put(rs.getString("id"),
						rs.getString("isDeleted"));
			}
			if (rs.wasNull()) {
				System.out
				.println("WriteToTwitterStreamArchiveSQL: No deletion notices in the table \"deletionNotices\"");
			} else {
				System.out.println("WriteToTwitterStreamArchiveSQL: Found "
						+ deletionNotices.size() + " dNotices.");
			}
		} catch (SQLException e) {
			System.err.println("WriteToTwitterStreamArchiveSQL: e:"
					+ e.toString());
		}
	}

	/**
	 * Add new deletion notice to the database and to the deletionNotices class
	 * field.
	 * 
	 * @param tweetId
	 *            of the tweet to delete
	 * @param deleted
	 *            whether the tweet has been deleted or not
	 * @param userId
	 *            of the user that created the tweet to be deleted
	 */
	private void storeDeletionNotice(String tweetId, String deleted,
			String userId) {
		System.out.println("tweetid=" + tweetId + " deleted=" + deleted
				+ " userId=" + userId);
		if (notInDB("dNotices", "id", tweetId)) {
			String sqlString = tweetId + ", " + userId + ", " + deleted;
			try {
				stmt.executeUpdate("insert into dNotices values (" + sqlString
						+ ")");
			} catch (SQLException e) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: Insert into dNotices failed.");
				System.err.println(sqlString);
				e.printStackTrace();
				System.exit(-1);
			}

		} else {
			System.out
			.println("WriteToTwitterStreamArchiveSQL: Deletion notice already in dNotices table.");
		}
		deletionNotices.put(tweetId, deleted);
	}

	/**
	 * Update the deletion notice status to deleted in the database and the
	 * deletionNotices class field.
	 * 
	 * @param tweetId
	 *            of the deleted tweet
	 */
	private void updateDeletionNotice(String tweetId) {
		// sets deletion notice to deleted
		deletionNotices.put(tweetId, "true");
		// update in database as well
		if (notInDB("dNotices", "id", tweetId)) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Deletion notice not found in dNotices table.");
			System.exit(-1);
		} else {
			try {
				stmt.executeUpdate("update dNotices SET isDeleted=1");
			} catch (SQLException e) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: failed to update DeletionNotice with isDeleted=1");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add tweet into database from raw json string format tweet. (Used by
	 * TwitterDiskToSQL)
	 * 
	 * @param tweetString
	 *            a json string tweet
	 */
	public void tweetToSQL(String tweetString) {
		HashMap<String, String> tweet = twitterFields.parseTweet(tweetString);
		TUser user = twitterFields.parseUser((String) tweet.get("user"));
		tweetToSQL(tweet, user);
	}

	/**
	 * Add tweet into database from name, value pair <CODE>HashMap</CODE> format
	 * tweet. (Used by SearchImport and TwapperKeeperImport)
	 * 
	 * @param tweet
	 *            a <CODE>HashMap</CODE> name, value pair tweet
	 */
	public void tweetToSQL(HashMap<String, String> tweet) {
		TUser user = new TUser();
		tweetToSQL(tweet, user);
	}

	/**
	 * Add tweet into database from name, value pair <CODE>HashMap</CODE> format
	 * tweet and <CODE>TUser</CODE> object. If the tweet already exists, don't
	 * store it. Assumes that Stream API tweets are always imported before any
	 * other type of tweets. The streaming API has the fullest tweet data, and
	 * includes the user data. So if the stream API had the tweet, it should be
	 * in the database when it is also found from a different source. The second
	 * occurrence of the tweet isn't stored but the API Overlap table and
	 * SearchAPI tweet fields in the tweet table are updated.
	 * 
	 * @param tweet
	 *            name, value pair format tweet
	 * @param user
	 *            Twitter user object of author of tweet
	 */
	private void tweetToSQL(HashMap<String, String> tweet, TUser user) {
		// System.out.println(tweet.toString());
		// + tweet.get("createdAt"));
		// System.out.println("length: (" + user.get("timeZone") + ") "
		// + user.get("timeZone").length());
		// ------------------------
		// utcOffset has big number like 36000 or 34200 must be in minutes? but
		// sometime has "+1"

		if (deletionNotices.containsKey(tweet.get("id"))) {
			if (deletionNotices.get(tweet.get("id")).equals("1")) {
				if (theAPI.equals("Stream")) {
					// deletion notices only generated by Stream API
					// expect to find some tweets via other api's that also need
					// to be deleted, so don't report them as a problem.
					System.err
					.println("WriteToTwitterStreamArchiveSQL: tweet found when deletion notice already true!");
				}
			} else {
				updateDeletionNotice(tweet.get("id"));
			}
			System.out
			.println("WriteToTwitterStreamArchiveSQL: Deletion notice found, tweet not stored.");
		} else {
			// Might want to change it so if theAPI is Stream, and the tweet is
			// already there we still store it if the sourceAPI isn't Stream
			// replacing the one that is already there?
			// If it is Stream then we have had a duplication & maybe want to
			// report it?
			if (notInDB("tweets", "id", tweet.get("id"))) {
				storeTweetInDB(tweet, user);
			} else if (theAPI.equals("TwapperKeeper")
					|| theAPI.equals("Search")) {
				addSearchId(tweet.get("id"), tweet.get("fromUserIdSearch"),
						tweet.get("fromUserScreenName"),
						tweet.get("toUserIdSearch"),
						tweet.get("toUserScreenName"));
			}
			updateApiOverlap(tweet.get("id"));
			// at this stage only have user data if it is from the Stream API
			// and the "Status" api, but this code isn't called for the Status
			// API (it is used for finding missing users)
			if (theAPI.equals("Stream")
					&& notInDB("users", "id", Long.toString(user.getId()))) {
				storeUserInDB(user);
			}
		}
	}

	/**
	 * Add the SearchAPI fields in the existing record in the tweet table and
	 * update the searchAPIIds table
	 * 
	 * @param tweetId
	 *            id of the record to be updated
	 * @param fromUserIdSearch
	 *            SearchAPI user id of the sending user
	 * @param fromUserScreenName
	 *            SearchAPI screen name of the sending user
	 * @param toUserIdSearch
	 *            SearchAPI user id of the destination user (can be blank)
	 * @param toUserScreenName
	 *            SearchAPI user screen name of the destination user (can be
	 *            blank)
	 */
	private void addSearchId(String tweetId, String fromUserIdSearch,
			String fromUserScreenName, String toUserIdSearch,
			String toUserScreenName) {
		try {
			rs = stmt
					.executeQuery("select to_user_id, to_user_id_Search, to_user, from_user_id, from_user_id_Search, from_user, tweets.sourceAPI from tweets where id = "
							+ tweetId);
			if (rs.next()) {
				if (rs.getString("sourceAPI").equals("Stream")) {
					// There will only be a user record if the stored tweet is
					// from
					// Stream API
					String tweetFromUserId = rs.getString("from_user_id");
					String tweetToUserId = rs.getString("to_user_id");
					String tweetFromUserScreenName = rs.getString("from_user");
					String tweetToUserScreenName = rs.getString("to_user");
					int tweetFromUserIdSearch = rs
							.getInt("from_user_id_Search");
					int tweetToUserIdSearch = rs.getInt("to_user_id_Search");

					// check that fields that should match between tweets
					if (!tweetFromUserScreenName
							.equalsIgnoreCase(fromUserScreenName)) {
						System.err
						.println("Stored tweetFromUserScreenName ("
								+ tweetFromUserScreenName
								+ ") doesn't match new tweet fromUserScreenName ("
								+ fromUserScreenName + ")");
					}
					if (toUserScreenName != null) {
						System.out.println("toUserScreenName: "
								+ toUserScreenName);
						if (!tweetToUserScreenName
								.equalsIgnoreCase(toUserScreenName)) {
							System.err
							.println("Stored tweetToUserScreenName ("
									+ tweetToUserScreenName
									+ ") doesn't match new tweet toUserScreenName ("
									+ toUserScreenName + ")");
						}
					}

					updateSearchAPIsTable(tweetFromUserId, fromUserScreenName);
					if (toUserScreenName != null) {
						updateSearchAPIsTable(tweetToUserId, toUserScreenName);
						if (toUserIdSearch != null
								&& !toUserIdSearch.equals("0")) {
							// we have a new toUserIdSearch
							if (tweetToUserIdSearch != 0) {
								if (tweetToUserIdSearch != Integer
										.parseInt(toUserIdSearch)) {
									System.err
									.println("WriteToTwitterStreamArchiveSQL: new toUserIdSearch ("
											+ toUserIdSearch
											+ ") doesn't match tweetToUserIdSearch ("
											+ tweetToUserIdSearch
											+ ") for tweet id: "
											+ tweetId);
								}
							} else {
								// set toUserSearchId
								stmt.executeUpdate("update tweets SET to_user_id_Search='"
										+ toUserIdSearch
										+ "' where id="
										+ tweetId);
							}
						}
					}

					if (fromUserIdSearch != null
							&& !fromUserIdSearch.equals("0")) {
						if (tweetFromUserIdSearch != 0) {
							if (tweetFromUserIdSearch != Integer
									.parseInt(fromUserIdSearch)) {
								System.err
								.println("WriteToTwitterStreamArchiveSQL: new fromUserIdSearch ("
										+ fromUserIdSearch
										+ ") doesn't match tweetFromUserIdSearch ("
										+ tweetFromUserIdSearch
										+ ") for tweet id: " + tweetId);
							}
						} else {
							// set fromUserSearchId
							stmt.executeUpdate("update tweets SET from_user_id_Search='"
									+ fromUserIdSearch
									+ "' where id="
									+ tweetId);
						}
					}
				}
			} else {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: adding search ids to tweet record failed - no tweet found for tweet id: "
						+ tweetId);
			}

		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: adding search ids to user record failed.");
			e.printStackTrace();
		}
	}

	/**
	 * Update the SearchAPIids table which holds the searchUserId's and
	 * screenNames.
	 * 
	 * @param searchUserId
	 *            the user id from the searchAPI
	 * @param screenName
	 *            the user screen name (from searchAPI or TwapperKeeper).
	 */
	private void updateSearchAPIsTable(String searchUserId, String screenName) {
		// get the unique SearchAPIids record for the searchUserId
		try {
			rs = stmt
					.executeQuery("select * from SearchAPIids where searchUserId = "
							+ searchUserId);
			if (rs.next()) {
				// record exists
				String searchAPIScreenName = rs.getString("screenName");
				// check if screen name is the same. It sometimes isn't, which
				// is strange.
				if (!searchAPIScreenName.equalsIgnoreCase(screenName)) {
					System.out
					.println("WriteToTwitterStreamArchiveSQL: stored searchAPIScreenName ("
							+ searchAPIScreenName
							+ ") doesnt' match new tweet fromUserScreenName ("
							+ screenName + ")");
					try {
						stmt.executeUpdate("update SearchAPIids set screenName='"
								+ screenName
								+ "' where searchUserId="
								+ searchUserId);
					} catch (SQLException e) {
						System.err
						.println("WriteToTwitterStreamArchiveSQL: Update screen name in SearchAPIids failed.");
						e.printStackTrace();
						System.exit(-1);
					}
				}
			} else {
				// record doesn't exist, so add the new SearchAPIids record
				String sqlString = searchUserId + ", '" + screenName + "', '"
						+ theAPI + "'";

				try {
					stmt.executeUpdate("insert into SearchAPIids values  ("
							+ sqlString + ")");
				} catch (SQLException e) {
					System.err
					.println("WriteToTwitterStreamArchiveSQL: Insert into SearchAPIids failed.");
					System.err.println(sqlString);
					e.printStackTrace();
					System.exit(-1);
				}
			}
		} catch (SQLException e1) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: searching SearchAPIids for searchUserId="
					+ searchUserId + " failed.");
			e1.printStackTrace();
		}
	}

	/**
	 * Check for record in sql table with field = value.
	 * 
	 * @param table
	 *            name of the table to search
	 * @param field
	 *            field to search
	 * @param value
	 *            value to look for in field
	 * @return <CODE>true</CODE> if record found, <CODE>false</CODE> otherwise
	 */
	public Boolean notInDB(String table, String field, String value) {
		// external use of this by GetMissingTwitterUsers is why it is public
		try {
			rs = stmt.executeQuery("select " + field + " from " + table
					+ " where " + field + " = " + value);
		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: checking for record in "
					+ table
					+ " table where "
					+ field
					+ " = "
					+ value
					+ " failed at query.");
			e.printStackTrace();
		}
		Boolean inDb = true;
		try {
			inDb = rs.next();
		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: checking for record in "
					+ table
					+ " table where "
					+ field
					+ " = "
					+ value
					+ " failed at rs.next().");
			e.printStackTrace();
		}
		return !inDb;
	}

	/**
	 * Add a tweet into the tweets table
	 * 
	 * @param tweet
	 *            name, value pair format tweet
	 * @param user
	 *            Twitter user object of author of tweet
	 */
	private void storeTweetInDB(HashMap<String, String> tweet, TUser user) {
		// TODO: if change to updating the tweet in GetMissingUsers then need to
		// update this with test for theAPI.equals("Status") as being equivalent
		// to "Stream"

		String myLatitude = "0";
		String myLongitude = "0";
		int hasGeoCode = 0;
		Date tempCreatedAt = null;
		if (theAPI.equals("Stream")) {
			if (tweet.get("GeoLocation") != null) {
				System.out.println("GeoLocation: " + tweet.get("GeoLocation"));
				myLatitude = twitterFields.parseLatitude(tweet
						.get("GeoLocation"));
				myLongitude = twitterFields.parseLongitude(tweet
						.get("GeoLocation"));
				hasGeoCode = 1;
			}

			SimpleDateFormat myFormatterIn = new SimpleDateFormat(
					"EEE MMM dd HH:mm:ss z yyyy");
			// myFormatterIn = new
			// SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss z");
			try {
				tempCreatedAt = myFormatterIn.parse(tweet.get("createdAt"));
			} catch (ParseException e1) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: parsing created at date failed. (Stream API)");
				e1.printStackTrace();
				System.exit(-1);
				// use current datetime instead
				tempCreatedAt = new Date();
			}

			// check that we haven't missed any records
			checkTimeGap(tempCreatedAt, tweet.get("id"));

		} else if (theAPI.equals("TwapperKeeper")) {
			if (tweet.get("geoType") != null) {
				myLatitude = tweet.get("latitude");
				myLongitude = tweet.get("longitude");
				hasGeoCode = 1;
			}
			tempCreatedAt = new Date(cleanNumber(tweet.get("unixTime")) * 1000);
		} else if (theAPI.equals("Search")) {
			hasGeoCode = (int) cleanNumber(tweet.get("hasGeoCode"));
			if (hasGeoCode == 1) {
				myLatitude = tweet.get("latitude");
				myLongitude = tweet.get("longitude");
			}
			// CreatedAT: Mon, 16 Nov 2009 08:30:31 +0000
			SimpleDateFormat myFormatterIn = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss z");
			try {
				tempCreatedAt = myFormatterIn.parse(tweet.get("createdAt"));
			} catch (ParseException e1) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: parsing created at date failed (Search API).");
				e1.printStackTrace();
				System.exit(-1);
				// use current datetime instead
				tempCreatedAt = new Date();
			}

		}

		String fromUserId = "";
		String screenName = "";
		if (theAPI.equals("Stream")) {
			fromUserId = Long.toString(user.getId());
			screenName = user.getScreenName();
		} else if (theAPI.equals("TwapperKeeper") || theAPI.equals("Search")) {
			screenName = tweet.get("fromUserScreenName");
		}
		String sqlString = tweet.get("id") + ", '";
		sqlString += cleanText(tweet.get("source")) + "', '";
		sqlString += cleanText(tweet.get("text"), 450) + "', '";
		sqlString += tweet.get("createdAt") + "', ";
		sqlString += "STR_TO_DATE('" + GMTDateForSQL(tempCreatedAt)
				+ "', '%d %M %Y %H:%i:%s'), '";
		sqlString += cleanNumber(tweet.get("inReplyToUserId")) + "', '";
		sqlString += cleanNumber(tweet.get("toUserIdSearch")) + "', '";
		sqlString += cleanText(tweet.get("inReplyToScreenName")) + "', '";
		sqlString += cleanNumber(fromUserId) + "', '";
		sqlString += cleanNumber(tweet.get("fromUserIdSearch")) + "', '";
		sqlString += cleanText(screenName) + "', '";
		sqlString += hasGeoCode + "',";
		sqlString += myLatitude + ", ";
		sqlString += myLongitude + ", '";
		sqlString += cleanNumber(tweet.get("isTruncated")) + "', '";
		sqlString += cleanNumber(tweet.get("inReplyToStatusId")) + "', '";
		sqlString += cleanNumber(tweet.get("retweetedStatus")) + "', '";
		sqlString += cleanNumber(tweet.get("retweetedId")) + "', '";
		sqlString += cleanText(tweet.get("contributors")) + "', '";
		sqlString += cleanText(tweet.get("place")) + "', '";
		sqlString += cleanNumber(tweet.get("isFavorited")) + "', '";
		// TODO add new fields to database
		// new fields
		// Because I'm getting the tweets as a stream when they are created, the
		// retweet count will be 0 or low, so no point saving it
		// sqlString += cleanNumber(tweet.get("retweetCount")) + "', '";
		// sqlString += cleanNumber(tweet.get("wasRetweetedByMe") + "', '";
		// annotations not live yet, but will be between 512k and 2k each - so
		// need to go into a different table, not the main tweet table
		// sqlString += cleanText(tweet.get("annotations")) + "', '";

		// Storing entity fields in main database was a mistake - they are too
		// large.
		// If I decide I need them I need to create separate tables for them.
		// Until then just discard them.

		// sqlString += cleanText(tweet.get("userMentionEntities")) + "', '";
		// sqlString += cleanText(tweet.get("urlEntities")) + "', '";
		// sqlString += cleanText(tweet.get("hashtagEntities")) + "', '";
		// end new fields
		sqlString += theAPI + "', now())";
		// System.out.println("sqlString");
		try {
			stmt.executeUpdate("insert into tweets values  (" + sqlString);
		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Insert into tweets failed.");
			System.err.println(sqlString);
			System.out.println("textLength: "
					+ cleanText(tweet.get("text")).length());
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Add a Twitter user into the users table
	 * 
	 * @param user
	 *            Twitter user object of author of tweet
	 */
	public void storeUserInDB(TUser user) {
		// external use of this by GetMissingTwitterUsers is why it is public

		// Added storing theAPI into the user database.
		// until now all users have been Stream api, now starting to get some
		// with "Status" api from GetMissingTwitterUsers
		Date tempCreatedAt = user.getCreatedAt();
		String sqlString = user.getId() + ", '" + cleanText(user.getName())
				+ "', '" + cleanText(user.getScreenName()) + "', '"
				+ cleanText(user.getLocation(), 110) + "', '"
				+ cleanText(user.getDescription(), 495) + "', '"
				+ cleanText(user.getProfileImageUrl()) + "', '"
				// TODO: Might want to increase size of database field instead of truncating.
				// first record with URL longer than 255 was on 20/7/2012.
				+ cleanText(user.getUrl(),255) + "', '"
				+ cleanNumber(user.getIsProtected().toString()) + "', '"
				+ user.getFollowersCount() + "', '"
				+ cleanText(user.getStatus()) + "', '"
				+ cleanText(user.getProfileBackgroundColor(), 6) + "', '"
				+ cleanText(user.getProfileTextColor(), 6) + "', '"
				+ cleanText(user.getProfileLinkColor(), 6) + "', '"
				+ cleanText(user.getProfileSidebarFillColor(), 6) + "', '"
				+ cleanText(user.getProfileSidebarBorderColor(), 6) + "', '"
				+ user.getFriendsCount() + "', " + "STR_TO_DATE('"
				+ GMTDateForSQL(tempCreatedAt) + "', '%d %M %Y %H:%i:%s'), '"
				+ user.getFavouritesCount() + "', '"
				+ user.getUtcOffset()
				+ "', '"
				+ cleanText(user.getTimeZone())
				+ "', '"
				+ cleanText(user.getProfileBackgroundImageUrl(), 315)
				+ "', '"
				+ cleanNumber(user.getIsProfileBackgroundTiled().toString())
				+ "', '"
				+ user.getStatusesCount()
				+ "', '"
				+ cleanNumber(user.getIsGeoEnabled().toString())
				+ "', '"
				+ cleanNumber(user.getIsVerified().toString())
				+ "', '"
				// new fields
				// listedCount int
				+ user.getListedCount() + "', '"
				+ cleanText(user.getLang())
				+ "', '"
				// contributorsEnabled boolean
				+ cleanNumber(user.getIsContributorsEnabled().toString())
				+ "', '"
				// useProfileBackgroundImage boolean
				+ cleanNumber(user.getIsProfileUseBackgroundImage().toString())
				+ "', '"
				// showInlineMedia boolean
				+ cleanNumber(user.getIsShowAllInLineMedia().toString())
				// isTranslator boolean
				+ "', '" + cleanNumber(user.getIsTranslator().toString())
				// end new fields
				+ "', " + "now(), '" + theAPI + "')";
		try {
			stmt.executeUpdate("insert into users values  (" + sqlString);
		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Insert into users failed.");
			try {

				System.err
				.println("WriteToTwitterStreamArchiveSQL: Description length is: "
						+ cleanText(user.getDescription(), 495)
						.length()
						+ " bytes is: "
						+ cleanText(user.getDescription(), 495)
						.getBytes("UTF-8").length);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			System.err.println(sqlString);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Clean special characters from in the text and truncate it to length long
	 * if it is too long
	 * 
	 * @param text
	 * @param length
	 * @return truncated and cleaned text
	 */
	private String cleanText(String text, int length) {
		text = cleanText(text);
		try {
			if (text.getBytes("UTF-8").length > length) {
				// text = text.substring(0, length);
				text = truncateWhenUTF8(text, length);
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		return text;
	}

	/**
	 * Clean special characters from in the text
	 * 
	 * @param text
	 * @return cleaned text
	 */
	private String cleanText(String text) {
		if (text != null) {
			if (text.equals("null")) {
				text = "";
			}
			text = text.trim();
			text = text.replace("'", "%27");
			text = text.replace("\\", "%92");
		} else {
			text = "";
		}
		return text;
	}

	/**
	 * Convert text into number
	 * 
	 * @param text
	 *            string to convert into number
	 * @return number
	 */
	private long cleanNumber(String text) {
		long newLong = 0;
		if (text == null || text.equals("") || text.equals("null")) {
		} else if (text.equals("true")) {
			newLong = 1;
		} else if (text.equals("false")) {
			newLong = 0;
		} else {
			newLong = Long.parseLong(text);
		}
		return newLong;
	}

	/**
	 * Process a new deletion notice. Delete any existing tweet identified by
	 * the deletion notice and then store the deletion notice. Used by
	 * TwitterDiskToSQL.
	 * 
	 * @param deletionNotice
	 *            deletion notice as a json string
	 */
	public void processDeletionNotice(String deletionNotice) {
		String isDeleted = "0";
		String[] deletionFields = twitterFields
				.parseDeletionNotice(deletionNotice);
		if (!notInDB("tweets", "id", deletionFields[0])) {
			try {
				stmt.executeUpdate("delete from tweets where id="
						+ deletionFields[0]);
				isDeleted = "1";
				System.out
				.println("WriteToTwitterStreamArchiveSQL: Tweet has been deleted in response to deletion request.");
			} catch (SQLException e) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: Delete tweet from tweets where id is "
						+ deletionFields[0] + " failed.");
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			isDeleted = "0";
		}
		storeDeletionNotice(deletionFields[0], isDeleted, deletionFields[1]);
	}

	/**
	 * Add a new track limitation notice into the trackLimitations table. Used
	 * by TwitterDiskToSQL.
	 * 
	 * @param trackLimitationNotice
	 *            track limitation notice as a json string
	 * @param fileDate
	 *            date and time of start of the hour of data containing the
	 *            track limitation notice
	 */
	public void processTrackLimitation(String trackLimitationNotice,
			Date fileDate) {
		String strDate;
		int numberLimited = twitterFields
				.parseTrackLimitation(trackLimitationNotice);
		// TODO: if the lastRecordDate is null check the last trackLimitation
		// notice stored and if it has the same number of tweets dropped treat
		// it as a duplicate.
		if (lastRecordDate != null) {
			strDate = GMTDateForSQL(lastRecordDate);
		} else {
			strDate = GMTDateForSQL(fileDate);
		}
		String sqlString = "0, " + numberLimited + ", STR_TO_DATE('" + strDate
				+ "', '%d %M %Y %H:%i:%s'), " + lastRecordTweetId;
		try {
			stmt.executeUpdate("insert into trackLimitations values ("
					+ sqlString + ")");
			System.out
			.println("WriteToTwitterStreamArchiveSQL: Track limitation notice added to trackLimitations - "
					+ sqlString);
		} catch (SQLException e) {
			System.err
			.println("WriteToTwitterStreamArchiveSQL: Adding track limitation notice failed: "
					+ sqlString);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Update the apiOverlap table with the API type currently being processed.
	 * 
	 * @param tweetId
	 *            id of tweet to update
	 */
	private void updateApiOverlap(String tweetId) {
		String fieldName = "in" + theAPI;
		if (notInDB("apiOverlap", "tweetId", tweetId)) {
			String sqlString = tweetId + ", ";
			if (theAPI.equals("Stream")) {
				sqlString += 1 + ", " + 0 + ", " + 0;
			} else if (theAPI.equals("Search")) {
				sqlString += 0 + ", " + 1 + ", " + 0;
			} else if (theAPI.equals("TwapperKeeper")) {
				sqlString += 0 + ", " + 0 + ", " + 1;
			} else {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: Unknown API type: "
						+ theAPI);
				sqlString += 0 + ", " + 0 + ", " + 0;
				// not sure need to exit here...
				System.exit(-1);
			}
			try {
				stmt.executeUpdate("insert into apiOverlap values  ("
						+ sqlString + ")");
			} catch (SQLException e) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: Insert into apiOverlap failed.");
				System.err.println(sqlString);
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			try {
				stmt.executeUpdate("update apiOverlap SET " + fieldName
						+ "=1 where tweetId=" + tweetId);
			} catch (SQLException e) {
				System.err
				.println("WriteToTwitterStreamArchiveSQL: failded to update apiOverlap with "
						+ fieldName + " =true where tweetId=" + tweetId);
				e.printStackTrace();
			}
		}

	}

	/**
	 * Check the gap between records and add any gaps above 60 seconds to
	 * dataGaps table.
	 * 
	 * @param recordDate
	 *            date of current tweet record
	 * @param tweetId
	 *            Twitter tweet id of current tweet record
	 */
	private void checkTimeGap(Date recordDate, String tweetId) {
		// maximum gap in seconds
		long maxGap = 60;
		if (lastRecordDate != null) {
			// gap in seconds
			long gap = (recordDate.getTime() - lastRecordDate.getTime()) / 1000;
			if (gap > maxGap) {
				String sqlString = "0,";
				sqlString += "STR_TO_DATE('" + GMTDateForSQL(lastRecordDate)
						+ "', '%d %M %Y %H:%i:%s'), ";
				sqlString += "STR_TO_DATE('" + GMTDateForSQL(recordDate)
						+ "', '%d %M %Y %H:%i:%s'), '";
				sqlString += lastRecordTweetId + "', '" + tweetId + "',";
				sqlString += gap;
				System.out
				.println("WriteToTwitterStreamArchiveSQL: Gap between records > "
						+ maxGap
						+ " seconds. Gap was: "
						+ gap
						+ " seconds.");
				System.out
				.println("WriteToTwitterStreamArchiveSQL: Gap between : "
						+ lastRecordDate.toString()
						+ " ("
						+ lastRecordTweetId
						+ ")"
						+ " and "
						+ recordDate.toString() + " (" + tweetId + ")");

				try {
					stmt.executeUpdate("insert into dataGaps values  ("
							+ sqlString + ")");
				} catch (SQLException e) {
					System.err
					.println("WriteToTwitterStreamArchiveSQL: Insert into dataGaps failed.");
					System.err.println(sqlString);
					e.printStackTrace();
					System.exit(-1);
				}

			} else {
				// System.out.println("WriteToTwitterStreamArchiveSQL: Gap between records < "
				// + maxGap + " seconds. Gap was: " + gap + " seconds.");
			}
		}
		// update the lastRecordDate
		lastRecordDate = recordDate;
		lastRecordTweetId = tweetId;
	}

	/**
	 * Convert date into GMT date string suitable for mySQL insertion.
	 * 
	 * @param date
	 *            java <Date> object to be converted
	 * @return GMT date string
	 */
	private String GMTDateForSQL(Date date) {
		// set the output format for GMT conversion
		SimpleDateFormat myFormatterOut = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss Z");
		myFormatterOut.setTimeZone(TimeZone.getTimeZone("GMT"));

		String myDate = myFormatterOut.format(date, new StringBuffer(),
				new FieldPosition(0)).toString();
		String myDateGMT = myDate.substring(5, myDate.length() - 6);
		return myDateGMT;
	}

	/**
	 * Code from StackOverflow
	 * http://stackoverflow.com/questions/119328/how-do-i
	 * -truncate-a-java-string-to-fit-in-a-given-number-of-bytes-once-utf-8-enc
	 * Introduced to properly truncate fields to byte lengths for varchar fields
	 * in mySQL
	 * 
	 * @param s
	 * @param maxBytes
	 * @return
	 */
	private static String truncateWhenUTF8(String s, int maxBytes) {
		int b = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			// ranges from http://en.wikipedia.org/wiki/UTF-8
			int skip = 0;
			int more;
			if (c <= 0x007f) {
				more = 1;
			} else if (c <= 0x07FF) {
				more = 2;
			} else if (c <= 0xd7ff) {
				more = 3;
			} else if (c <= 0xDFFF) {
				// surrogate area, consume next char as well
				more = 4;
				skip = 1;
			} else {
				more = 3;
			}

			if (b + more > maxBytes) {
				return s.substring(0, i);
			}
			b += more;
			i += skip;
		}
		return s;
	}

}
