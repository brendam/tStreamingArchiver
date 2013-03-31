package au.net.moon.tUtils;
/**
 * TUser - data structure to hold a Twitter user record
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import twitter4j.User;

/**
 * Twitter User object.
 * 
 * When this class was written twitter4j didn't have any way to create a user
 * object except by getting it from twitter. Looks like it might be possible to
 * use twitter4j.json.DataObjectFactory to create a user record from a JSON
 * string since Twitter4J 2.1.7. So it might now be possible to just use the
 * twitter4j classes.
 * 
 * @author Brenda Moon - brenda at moon.net.au
 */
public class TUser {
	private long id;
	private String name;
	private String screenName;
	private String location;
	private String description;
	private String url;
	private Date createdAt;
	private String status;
	private String timeZone;
	private String profileImageUrl;
	private String profileBackgroundImageUrl;
	private String profileBackgroundColor;
	private String profileTextColor;
	private String profileLinkColor;
	private String profileSidebarFillColor;
	private String profileSidebarBorderColor;
	private Boolean isProfileBackgroundTiled;
	private int followersCount;
	private int friendsCount;
	private int favouritesCount;
	private int utcOffset;
	private int statusesCount;
	private Boolean isGeoEnabled;
	private Boolean isVerified;
	// Boolean isFollowing;
	private Boolean isProtected;
	private String searchAPI_userID;
	private int listedCount;
	private String lang;
	private Boolean isContributorsEnabled;
	private Boolean isProfileUseBackgroundImage;
	private Boolean isShowAllInLineMedia;
	private Boolean isTranslator;
	private Boolean isFollowRequestSent;

	public TUser() {
	}

	/**
	 * Create a user object from a name, value pair <CODE>HashMap</CODE>
	 * 
	 * @param hmUser
	 *            the name, value pair formatted user
	 */
	public TUser(HashMap<String, String> hmUser) {
		id = Integer.parseInt(hmUser.get("id"));
		name = hmUser.get("name");
		screenName = hmUser.get("screenName");
		location = hmUser.get("location");
		description = hmUser.get("description");
		profileImageUrl = hmUser.get("profileImageURL");
		url = hmUser.get("url");
		if (hmUser.get("isProtected").equals("1")) {
			isProtected = true;
		} else {
			isProtected = false;
		}
		SimpleDateFormat myFormatterIn = new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss z yyyy");
		Date tempCreatedAt;
		try {
			tempCreatedAt = myFormatterIn.parse(hmUser.get("createdAt"));
		} catch (ParseException e1) {
			System.err
					.println("TwitterDiskToSQL: parsing created at date failed.");
			e1.printStackTrace();
			// use current datetime instead
			tempCreatedAt = new Date();
		}

		createdAt = tempCreatedAt;
		followersCount = Integer.parseInt(hmUser.get("followersCount"));
		// Not interested in users latest tweet, so just leave blank
		status = "";
		profileBackgroundColor = hmUser.get("profileBackgroundColor");
		profileTextColor = hmUser.get("profileTextColor");
		profileLinkColor = hmUser.get("profileLinkColor");
		profileSidebarFillColor = hmUser.get("profileSidebarFillColor");
		profileSidebarBorderColor = hmUser.get("profileSidebarBorderColor");
		friendsCount = Integer.parseInt(hmUser.get("friendsCount"));
		favouritesCount = Integer.parseInt(hmUser.get("favouritesCount"));
		utcOffset = Integer.parseInt(hmUser.get("utcOffset"));
		timeZone = hmUser.get("timeZone");
		profileBackgroundImageUrl = hmUser.get("profileBackgroundImageUrl");
		if (hmUser.get("profileBackgroundTiled").equals("1")) {
			isProfileBackgroundTiled = true;
		} else {
			isProfileBackgroundTiled = false;
		}
		statusesCount = Integer.parseInt(hmUser.get("statusesCount"));
		if (hmUser.get("isProtected").equals("1")) {
			isProtected = true;
		} else {
			isProtected = false;
		}
		if (hmUser.get("isGeoEnabled").equals("1")) {
			isGeoEnabled = true;
		} else {
			isGeoEnabled = false;
		}
		if (hmUser.get("isVerified").equals("1")) {
			isVerified = true;
		} else {
			isVerified = false;
		}
		listedCount = Integer.parseInt(hmUser.get("listedCount"));
		lang = hmUser.get("lang");
		if (hmUser.get("isGeoEnabled").equals("1")) {
			isGeoEnabled = true;
		} else {
			isGeoEnabled = false;
		}
		if (hmUser.get("isContributorsEnabled").equals("1")) {
			isContributorsEnabled = true;
		} else {
			isContributorsEnabled = false;
		}
		if (hmUser.get("profileUseBackgroundImage").equals("1")) {
			isProfileUseBackgroundImage = true;
		} else {
			isProfileUseBackgroundImage = false;
		}
		if (hmUser.get("showAllInlineMedia").equals("1")) {
			isShowAllInLineMedia = true;
		} else {
			isShowAllInLineMedia = false;
		}
		if (hmUser.get("isFollowRequestSent").equals("1")) {
			isFollowRequestSent = true;
		} else {
			isFollowRequestSent = false;
		}

		if (hmUser.get("translator").equals("1")) {
			isTranslator = true;
		} else {
			isTranslator = false;
		}

		// TODO: Need to pass in the searchAPI id if I have it.
		searchAPI_userID = "";
	}

	/**
	 * Create a user object from a twitter4j user.
	 * 
	 * @param twitterUser
	 *            a twitter4j user object
	 */
	public TUser(twitter4j.User twitterUser) {
		id = twitterUser.getId();
		name = twitterUser.getName();
		screenName = twitterUser.getScreenName();
		location = twitterUser.getLocation();
		description = twitterUser.getDescription();
		profileImageUrl = twitterUser.getProfileImageURL().toString();
		if (twitterUser.getURL() != null) {
			url = twitterUser.getURL().toString();
		} else {
			url = "";
		}
		createdAt = twitterUser.getCreatedAt();
		isProtected = twitterUser.isProtected();
		followersCount = twitterUser.getFollowersCount();
		// Not interested in users latest tweet, so just leave blank
		status = "";
		profileBackgroundColor = twitterUser.getProfileBackgroundColor();
		profileTextColor = twitterUser.getProfileTextColor();
		profileLinkColor = twitterUser.getProfileLinkColor();
		profileSidebarFillColor = twitterUser.getProfileSidebarFillColor();
		profileSidebarBorderColor = twitterUser.getProfileSidebarBorderColor();
		friendsCount = twitterUser.getFriendsCount();
		favouritesCount = twitterUser.getFavouritesCount();
		utcOffset = twitterUser.getUtcOffset();
		timeZone = twitterUser.getTimeZone();
		profileBackgroundImageUrl = twitterUser.getOriginalProfileImageURL()
				.toString();
		isProfileBackgroundTiled = twitterUser.isProfileBackgroundTiled();
		statusesCount = twitterUser.getStatusesCount();
		isGeoEnabled = twitterUser.isGeoEnabled();
		isVerified = twitterUser.isVerified();
		// isFollowing = twitterUser.isFollowing();

		isContributorsEnabled = twitterUser.isContributorsEnabled();
		isProfileUseBackgroundImage = twitterUser.isProfileUseBackgroundImage();
		isShowAllInLineMedia = twitterUser.isShowAllInlineMedia();
		lang = twitterUser.getLang();
		isTranslator = twitterUser.isTranslator();
		listedCount = twitterUser.getListedCount();
		isFollowRequestSent = twitterUser.isFollowRequestSent();

		// TODO: Need to pass in the searchAPI id if I have it.
		searchAPI_userID = "";
	}

	/**
	 * Return the user id.
	 * 
	 * @return Twitter user id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Return the user name.
	 * 
	 * @return Twitter user name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the user screen name.
	 * 
	 * @return Twitter user screen name
	 */
	public String getScreenName() {
		return screenName;
	}

	/**
	 * Return the user location.
	 * 
	 * @return Twitter user location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Return the user description.
	 * 
	 * @return Twitter user description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the user profile image url.
	 * 
	 * @return Twitter user profile image url
	 */
	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	/**
	 * Return the user url.
	 * 
	 * @return Twitter user url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Return the user protected status.
	 * 
	 * @return Twitter user protected status
	 */
	public Boolean getIsProtected() {
		return isProtected;
	}

	/**
	 * Return the user followers count.
	 * 
	 * @return Twitter user followers count
	 */
	public int getFollowersCount() {
		return followersCount;
	}

	/**
	 * Return the user's most recent tweet.
	 * 
	 * @return Twitter user's most recent tweet
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Return the user profile background colour.
	 * 
	 * @return Twitter user profile background colour
	 */
	public String getProfileBackgroundColor() {
		return profileBackgroundColor;
	}

	/**
	 * Return the user profile text colour.
	 * 
	 * @return Twitter user profile text colour
	 */
	public String getProfileTextColor() {
		return profileTextColor;
	}

	/**
	 * Return the user profile link colour.
	 * 
	 * @return Twitter user profile link colour
	 */
	public String getProfileLinkColor() {
		return profileLinkColor;
	}

	/**
	 * Return the user profile sidebar fill colour.
	 * 
	 * @return Twitter user profile sidebar fill colour
	 */
	public String getProfileSidebarFillColor() {
		return profileSidebarFillColor;
	}

	/**
	 * Return the user profile sidebar border colour.
	 * 
	 * @return Twitter user profile sidebar border colour
	 */
	public String getProfileSidebarBorderColor() {
		return profileSidebarBorderColor;
	}

	/**
	 * Return the user friends count.
	 * 
	 * @return Twitter user friends count
	 */
	public int getFriendsCount() {
		return friendsCount;
	}

	/**
	 * Return the user favourites count.
	 * 
	 * @return Twitter user favourites count
	 */
	public int getFavouritesCount() {
		return favouritesCount;
	}

	/**
	 * Return the user time offset from UTC.
	 * 
	 * @return Twitter user UTC offset
	 */
	public int getUtcOffset() {
		return utcOffset;
	}

	/**
	 * Return the user time zone.
	 * 
	 * @return Twitter user time zone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * Return the user profile background image url.
	 * 
	 * @return Twitter user profile background image url
	 */
	public String getProfileBackgroundImageUrl() {
		return profileBackgroundImageUrl;
	}

	/**
	 * Return whether the user profile background image is tiled.
	 * 
	 * @return is the background image tiled
	 */
	public Boolean getIsProfileBackgroundTiled() {
		return isProfileBackgroundTiled;
	}

	/**
	 * Return the user statuses count.
	 * 
	 * @return Twitter user statuses count
	 */
	public int getStatusesCount() {
		return statusesCount;
	}

	/**
	 * Determine if the user has geo location enabled
	 * 
	 * @return <CODE>true</CODE> if geolocation is enabled, <CODE>false</CODE>
	 *         otherwise
	 */
	public Boolean getIsGeoEnabled() {
		return isGeoEnabled;
	}

	/**
	 * Determine if the user is verified by Twitter
	 * 
	 * @return <CODE>true</CODE> if user is verified, <CODE>false</CODE>
	 *         otherwise
	 */
	public Boolean getIsVerified() {
		return isVerified;
	}

	// public Boolean getIsFollowing() {
	// return isFollowing;
	// }

	/**
	 * Return the searchAPI user id of the user.
	 * 
	 * @return searchAPI user id
	 */
	public String getSearchAPI_userID() {
		return searchAPI_userID;
	}

	/**
	 * Return the date the user was created on Twitter.
	 * 
	 * @return the date the user was created on Twitter
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	// new fields below here...
	// TODO: Still need to add to database & database saving code
	// (WriteToTwitterStreamArchive)

	/**
	 * Determine whether contributors are enabled. This is used for business
	 * accounts to let different users tweet through the main account. Twitter
	 * adds a by-line to the tweets.
	 * 
	 * @return <CODE>True</CODE> if contributors enabled, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean getIsContributorsEnabled() {
		return isContributorsEnabled;
	}

	/**
	 * Determine whether the profile should use a background image.
	 * 
	 * @return <CODE>True</CODE> if background image enabled, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean isProfileUseBackgroundImage() {
		return isProfileUseBackgroundImage;
	}

	/**
	 * Determine whether user has inline media enabled.
	 * 
	 * @return <CODE>True</CODE> if inline media enabled, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean isShowAllInlineMedia() {
		return isShowAllInLineMedia;
	}

	/**
	 * Return the prefered language of the user.
	 * 
	 * @return the prefered language
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * Determine whether user is a translator.
	 * 
	 * @return <CODE>True</CODE> if user is a translator, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean isTranslator() {
		return isTranslator;
	}

	/**
	 * Return number of times the user appears in lists.
	 * 
	 * @return count of lists user is in
	 */
	public int getListedCount() {
		return listedCount;
	}

	/**
	 * Determine whether a follow request has been sent.
	 * 
	 * @return <CODE>True</CODE> if follow request sent, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean isFollowRequestSent() {
		return isFollowRequestSent;
	}

	/**
	 * Determine whether a profile background image should be used.
	 * 
	 * @return <CODE>True</CODE> to use image, <CODE>False</CODE> otherwise
	 */
	public Boolean getIsProfileUseBackgroundImage() {
		return isProfileUseBackgroundImage;
	}

	/**
	 * Determine whether inline media should be shown.
	 * 
	 * @return <CODE>True</CODE> to show inline media, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean getIsShowAllInLineMedia() {
		return isShowAllInLineMedia;
	}

	/**
	 * Determine whether a user is a translator.
	 * 
	 * @return <CODE>True</CODE> if user is a translator, <CODE>False</CODE>
	 *         otherwise
	 */
	public Boolean getIsTranslator() {
		return isTranslator;
	}

}
