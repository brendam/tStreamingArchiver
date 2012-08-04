package au.net.moon.tGetMissingUsers;
/**
 * ReadTwitter - Get a tweet from Twitter with embedded user data
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

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import au.net.moon.tUtils.TUser;
import au.net.moon.tUtils.twitterAuthorise;

/**
 * Get a tweet from twitter. Re-request the tweet that has the missing user and
 * get the user out of it. This is the only way to be confident that it is the
 * right user (twitter user id's are different in searchAPI than main API).
 * http://api.twitter.com/version/statuses/show/:id.format
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class ReadTwitter {
	private Twitter twitter;

	ReadTwitter() {
		twitterAuthorise twitterAuth = new twitterAuthorise(false);
		twitter = twitterAuth.getTwitter();
		if (twitter == null) {
			System.err.println("ReadTwitter: Twitter not connected");
			System.exit(-1);
		}
	}

	/**
	 * Get the user by getting a tweet with the user embedded in it.
	 * 
	 * @param tweetId
	 *            the tweet to retrieve
	 * @return Twitter user object
	 */
	public TUser getUserFromTweet(long tweetId) {
		TUser user = null;
		try {
			Status tweet = twitter.showStatus(tweetId);
			user = new TUser(tweet.getUser());
		} catch (TwitterException e) {
			if (e.getStatusCode() == 404 || e.getStatusCode() == 403) {
				// 404 = tweet not found
				// 403 = tweet protected or user account suspended
				// just return the null user
			} else {
				e.printStackTrace();
			}
		}
		return user;
	}
}
