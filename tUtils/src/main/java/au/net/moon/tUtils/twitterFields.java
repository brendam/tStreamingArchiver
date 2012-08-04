package au.net.moon.tUtils;
/**
 * twitterFields - Define the fields in the raw Twitter data json text string and extract them.
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

import java.util.HashMap;

import au.net.moon.tUtils.TUser;

/**
 * Define the fields in the raw Twitter data json text string and extract them.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 */
// new Tweet fields: "retweetCount", "wasRetweetedByMe", "annotations",
// "userMentionEntities", "urlEntities","hashtagEntities"
// new User fields: "isContributorsEnabled",
// "profileUseBackgroundImage","showAllInlineMedia","profileBackgroundTiled",
// "lang","isGeoEnabled", "isVerified", "translator","listedCount",
// "isFollowRequestSent"
public class twitterFields {
	private static final String[] statusFields = { "createdAt", "id", "text",
			"source", "isTruncated", "inReplyToStatusId", "inReplyToUserId",
			"isFavorited", "inReplyToScreenName", "geoLocation", "place",
			"retweetCount", "wasRetweetedByMe", "contributors", "annotations",
			"retweetedStatus", "userMentionEntities", "urlEntities",
			"hashtagEntities", "user" };
	private static final String[] userFields = { "id", "name", "screenName",
			"location", "description", "isContributorsEnabled",
			"profileImageUrl", "url", "isProtected", "followersCount",
			"status", "profileBackgroundColor", "profileTextColor",
			"profileLinkColor", "profileSidebarFillColor",
			"profileSidebarBorderColor", "profileUseBackgroundImage",
			"showAllInlineMedia", "friendsCount", "createdAt",
			"favouritesCount", "utcOffset", "timeZone",
			"profileBackgroundImageUrl", "profileBackgroundTiled", "lang",
			"statusesCount", "isGeoEnabled", "isVerified", "translator",
			"listedCount", "isFollowRequestSent" };
	private final static String programName = "twitterFields";

	twitterFields() {
	}

	/**
	 * Check whether the current record is a tweet.
	 * 
	 * @param status
	 *            the json status string
	 * @return <CODE>true</CODE> if it is a normal Tweet, <CODE>false</CODE>
	 *         otherwise.
	 */
	public static Boolean isTweet(String status) {
		return status.startsWith("StatusJSONImpl");
	}

	/**
	 * Check whether the current record is a deletion notice.
	 * 
	 * @param status
	 *            the json status string
	 * @return <CODE>true</CODE> if it is a deletion notice, <CODE>false</CODE>
	 *         otherwise.
	 */
	public static Boolean isDeletionNotice(String status) {
		return status.startsWith("StatusDeletionNoticeImpl");
	}

	/**
	 * Check whether the current record is a track limitation notice.
	 * 
	 * @param status
	 *            the json status string
	 * @return <CODE>true</CODE> if it is a track limitation notice,
	 *         <CODE>false</CODE> otherwise.
	 */
	public static Boolean isTrackLimitation(String status) {
		return status.startsWith("TrackLimitationNotice");
	}

	/**
	 * Get the number of status that have been skipped from the track limitation
	 * notice.
	 * 
	 * @param status
	 *            the json status string
	 * @return the number of status that have been limited (skipped).
	 */
	public static int parseTrackLimitation(String status) {
		int numberLimited = 0;
		if (isTrackLimitation(status)) {
			numberLimited = Integer.parseInt(status.substring(status
					.indexOf("=") + 1));
		} else {
			System.err
					.println(programName
							+ ": parseTrackLimitation called with wrong status type : status: "
							+ status.substring(0, 40));
			status = "";
		}
		return numberLimited;
	}

	/**
	 * Get the tweetId and userId from a deletion notice.
	 * 
	 * @param status
	 *            the json status string
	 * @return the deletion notice as a two element <CODE>String</CODE> array
	 *         containing the tweetId and the userId.
	 */
	public static String[] parseDeletionNotice(String status) {
		String[] fields = new String[2];
		if (isDeletionNotice(status)) {
			String deletionNotice = status.replace(
					"StatusDeletionNoticeImpl{statusId=", "");
			String tweetId = deletionNotice.substring(0,
					deletionNotice.indexOf(","));
			String userId = deletionNotice.substring(deletionNotice
					.indexOf("userId=") + (new String("userId=").length()));
			userId = userId.replace("}", "");
			// System.out.println("userID: " + userId + " tweetId: " + tweetId);
			fields[0] = tweetId;
			fields[1] = userId;
		} else {
			System.err
					.println(programName
							+ ": parseDeletionNotice called with wrong status type : status: "
							+ status.substring(0, 40));
			fields[0] = "";
			fields[1] = "";
		}
		return fields;
	}

	/**
	 * Get a <CODE>HashMap</CODE> of tweet fields by parsing a JSON string
	 * tweet.
	 * 
	 * @param status
	 *            the json status string
	 * @return the tweet fields as name, value pairs in a <CODE>HashMap</CODE>.
	 */
	public static HashMap<String, String> parseTweet(String status) {
		HashMap<String, String> tweet = new HashMap<String, String>();
		if (isTweet(status)) {
			// parse the status here
			tweet = parseFields(status, statusFields, true);
		} else {
			System.err.println(programName
					+ ": parseTweet called with wrong status type : status: "
					+ status.substring(0, 40));
		}
		return tweet;
	}

	/**
	 * Get the Twitter user from a Twitter JSON string user field.
	 * 
	 * @param userString
	 *            Twitter JSON string user field
	 * @return Twitter user object.
	 */
	public static TUser parseUser(String userString) {
		TUser user = new TUser(parseFields(userString, userFields, false));
		return user;
	}

	/**
	 * Parse and return the requested fields from the statusString provided
	 * 
	 * @param statusString
	 *            a json status string (tweet or user)
	 * @param fields
	 *            to match
	 * @param isTweet
	 *            <CODE>true</CODE> if it is a tweet, <CODE>false</CODE> if it
	 *            is user json string
	 * @return the requested fields as name, value pairs in a
	 *         <CODE>HashMap</CODE>.
	 */
	public static HashMap<String, String> parseFields(String statusString,
			String[] fields, Boolean isTweet) {
		HashMap<String, String> splitFields = new HashMap<String, String>();

		// look for each field in sequence
		String postFix = "=";
		String preFix = "";
		// String statusString = status;
		int endOfField = 0;
		for (int i = 0; i < fields.length; i++) {
			int startPos = statusString.indexOf(preFix + fields[i] + postFix);
			if (startPos >= 0) {
				startPos += preFix.length() + fields[i].length()
						+ postFix.length();
				statusString = statusString.substring(startPos);
				// only the first field doesn't have a prefix
				if (i + 1 < fields.length) {
					String jsonImpl1 = "StatusJSONImpl{";
					if (statusString.startsWith(jsonImpl1)) {
						statusString = statusString.substring(jsonImpl1
								.length());
						preFix = "}, ";
					} else if (statusString.charAt(0) == '\'') {
						preFix = "', ";
						// cut off the leading quote
						statusString = statusString.substring(1);
						// } else if (statusString.charAt(0) == '[') {
						// preFix = "], ";
						// // cut off the leading bracket
						// statusString = statusString.substring(1);
					} else {
						// normal non quoted field
						preFix = ", ";
					}
					endOfField = statusString.indexOf(preFix + fields[i + 1]
							+ postFix);
				} else {
					if (isTweet) {
						// looking for the final bracket
						// last field should be the user field ??
						String jsonImpl = "UserJSONImpl{";
						if (statusString.startsWith(jsonImpl)) {
							statusString = statusString.substring(jsonImpl
									.length());
						} else {
							System.err.println(programName
									+ ": User field wasn't the last field!"
									+ statusString.substring(0, 40));
							System.exit(-1);
						}
						endOfField = statusString.lastIndexOf("}") - 1;
						if (endOfField == -1) {
							// the user field in retweets doesn't have a final
							// "}"
							endOfField = statusString.length();
						}
					} else {
						// getting the 'verified=' field from end of user
						endOfField = statusString.length();
					}
				}
				// process the field
				if (endOfField < 0) {
					// found a tweet that doesn't have the 'following=false'
					// field - stops at verified field
					System.out.println(programName + ":statusString: "
							+ statusString + " endOfField:" + endOfField);
					endOfField = statusString.length();
					System.exit(-1);
				}
				String tempStatus = statusString.substring(0, endOfField);
				if (fields[i].equals("retweetedStatus")) {
					// set my added field of the original tweet id
					if (!tempStatus.equals("null")) {
						// get the tweet id of the original tweet
						HashMap<String, String> retweet = parseFields(
								tempStatus, statusFields, true);
						splitFields.put("retweetedId",
								(String) retweet.get("id"));
						tempStatus = "true";
					} else {
						splitFields.put("retweetedId", "null");
						tempStatus = "false";
					}
				}
				splitFields.put(fields[i], tempStatus);
				// chop off the field we just processed
				statusString = statusString.substring(endOfField);
			} else {
				// if (fields[i] == "following") {
				// // System.out.println(programName
				// // + ": 'following' field was missing.");
				// splitFields.put(fields[i], "false");
				// } else {
				System.err.println(programName
						+ ": Field not found! Fieldname:" + fields[i]);
				System.err.println(programName + ": start of field ("
						+ fields[i] + "): "
						+ statusString.substring(0, startPos + 10));
				System.exit(-1);
				// }
			}
		}
		// proveRetweetsAreJustRepeats(splitFields);
		return splitFields;
	}

	/**
	 * Get the tweet longitude from the geoLocation field.
	 * 
	 * @param geoLocation
	 *            the full geoLocation string
	 * @return longitude part of string (or blank if no longitude set).
	 */
	public static String parseLongitude(String geoLocation) {
		int startPos = geoLocation.indexOf("longitude=");
		String longitude = "";
		if (startPos > -1) {
			startPos += (new String("longitude=")).length();
			longitude = geoLocation.substring(startPos,
					geoLocation.indexOf("}"));
		}
		return longitude;
	}

	/**
	 * Get the tweet latitude from the geoLocation field.
	 * 
	 * @param geoLocation
	 *            the full geoLocation string
	 * @return latitude part of string (or blank if no longitude set).
	 */
	public static String parseLatitude(String geoLocation) {
		int startPos = geoLocation.indexOf("latitude=");
		String latitude = "";
		if (startPos > -1) {
			startPos += (new String("latitude=")).length();
			latitude = geoLocation
					.substring(startPos, geoLocation.indexOf(","));
		}
		return latitude;
	}

	/**
	 * Clean up special characters from inside textfield to make parsing easier.
	 * 
	 * @param txtField
	 *            the text to be cleaned
	 * @return the cleaned text string.
	 */
	public static String fixTextField(String txtField) {
		if (txtField.endsWith("\\")) {
			txtField = txtField + "\\";
		}
		if (txtField.indexOf("'") >= 0) {
			txtField = txtField.replace("'", "%27");
		}
		if (txtField.indexOf(",") >= 0) {
			// txtField = txtField.replace(",", "%44");
		}
		if (txtField.indexOf("{") >= 0) {
			txtField = txtField.replace("{", "%7B");
		}
		if (txtField.indexOf("}") >= 0) {
			txtField = txtField.replace("}", "%7D");
		}
		return txtField;
	}

	// ***************************************************************
	// Methods below here are just for testing things
	//

	/**
	 * Show that the only thing to keep about a retweet is it's Twitter tweet
	 * id. The retweet text is always a subset of the original tweet text, so
	 * the original will have already appeared in the stream
	 * 
	 * @param tweet
	 *            .
	 * @deprecated only used for testing.
	 */
	private static void proveRetweetsAreJustRepeats(
			HashMap<String, String> tweet) {
		if (!tweet.get("retweetedStatus").equals("null")) {
			HashMap<String, String> temp = parseTweet((String) tweet
					.get("retweetedStatus"));
			String tweet1 = (String) tweet.get("text");
			String retweeted = (String) temp.get("text");
			tweet1 = "'" + tweet1.substring(tweet1.indexOf(":") + 2);
			if (tweet1.equals(retweeted) || tweet1.equals(retweeted + "'")) {
				// System.out.println("matches");
			} else {
				if (((String) tweet.get("isTruncated")).equals("false")) {
					System.out.println(programName + ": tweet:   " + tweet1
							+ "\nretweet: " + retweeted);
					System.out.println(programName + ": truncated: "
							+ tweet.get("isTruncated"));
				}
			}
		}

	}

	/**
	 * Debug checking for end braces.
	 * 
	 * @param tempStatus
	 * @param fields
	 * @param i
	 * @deprecated only used for testing.
	 */
	private static void checkEndsForBraces(String tempStatus, String[] fields,
			int i) {
		if (tempStatus.endsWith("}") && !fields[i].equals("user")
				&& !fields[i].equals("geoLocation")
				&& !fields[i].equals("place")) {
			System.out.println(programName + ": field: " + fields[i]);
		}

	}
}
