package au.net.moon.tUtils;
/**
 * twitterAuthorise - connect to Twitter user oauth
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

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Connect to Twitter using oauth.
 * Needs a twitter4j.properties file to exist in program directory or 
 * root of classpath directory with Twitter oath parameters set.
 * See twitter4j.properties.sample in distribution.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 * 
 */
public class twitterAuthorise {

	static Twitter twitter;
	static TwitterStream twitterStream;

	/**
	 * @param streamConnection
	 *            <CODE>True</CODE> if connection is to streamAPI,
	 *            <CODE>False</CODE> otherwise
	 */
	public twitterAuthorise(Boolean streamConnection) {
		if (!streamConnection) {
			if (twitter == null) {
				System.out
						.println("TwitterAuthorise: Connecting to Twitter with OAuth");
				twitter = new TwitterFactory().getInstance();
				try {
					System.out.println("TwitterAuthorise: rate limit status: "
							+ twitter.getRateLimitStatus());
				} catch (TwitterException e) {
					System.out
							.println("TwitterAuthorise: Failed to get rate limit status");
					e.printStackTrace();
				}
			}
		} else {
			if (twitterStream == null) {
				System.out
						.println("TwitterAuthorise: Connecting to Twitter Stream with OAuth");
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setUseSSL(true);
				Configuration conf = builder.build();
				twitterStream = new TwitterStreamFactory(conf)
				.getInstance();
				System.out
						.println("TwitterAuthorise: Stream authorisation status: "
								+ twitterStream.getAuthorization());

			}
		}
	}

	/**
	 * Return a Twitter connection.
	 * 
	 * @return <CODE>Twitter</CODE> connection
	 */
	public Twitter getTwitter() {
		return twitter;
	}

	/**
	 * Return a Twitter stream connection.
	 * 
	 * @return <CODE>TwitterStream</CODE> connection
	 */
	public TwitterStream getTwitterStream() {
		return twitterStream;
	}
}
