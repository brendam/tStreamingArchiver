# ************************************************************
# Sequel Pro SQL dump
# Version 3408
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: 127.0.0.1 (MySQL 5.0.51a-24+lenny5-log)
# Database: twitter_stream_archive
# Generation Time: 2012-06-04 11:57:12 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table apiOverlap
# ------------------------------------------------------------

DROP TABLE IF EXISTS `apiOverlap`;

CREATE TABLE `apiOverlap` (
  `tweetId` bigint(20) NOT NULL default '0',
  `inStream` tinyint(1) default NULL,
  `inSearch` tinyint(1) default NULL,
  `inTwapperKeeper` tinyint(1) default NULL,
  PRIMARY KEY  (`tweetId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table CoOccuranceList
# ------------------------------------------------------------

DROP TABLE IF EXISTS `CoOccuranceList`;

CREATE TABLE `CoOccuranceList` (
  `id` int(11) NOT NULL auto_increment,
  `searchId` int(11) default NULL,
  `date` date default NULL,
  `word1` varchar(30) default NULL,
  `word2` varchar(30) default NULL,
  `frequency` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `coListKey` (`searchId`,`date`,`word1`,`word2`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table dataGaps
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dataGaps`;

CREATE TABLE `dataGaps` (
  `id` int(11) NOT NULL auto_increment,
  `startGapTime` datetime default NULL,
  `endGapTime` datetime default NULL,
  `startGapTweetId` bigint(20) default NULL,
  `endGapTweetId` bigint(20) default NULL,
  `timeGap` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table dNotices
# ------------------------------------------------------------

DROP TABLE IF EXISTS `dNotices`;

CREATE TABLE `dNotices` (
  `id` bigint(20) NOT NULL default '0',
  `userId` bigint(20) default NULL,
  `isDeleted` tinyint(1) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table SearchAPIids
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SearchAPIids`;

CREATE TABLE `SearchAPIids` (
  `searchUserId` bigint(20) NOT NULL,
  `screenName` varchar(32) default NULL,
  `sourceAPI` varchar(20) default NULL,
  PRIMARY KEY  (`searchUserId`),
  KEY `screenName` (`screenName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table searches
# ------------------------------------------------------------

DROP TABLE IF EXISTS `searches`;

CREATE TABLE `searches` (
  `id` int(11) NOT NULL auto_increment,
  `query` varchar(140) NOT NULL,
  `active` tinyint(1) NOT NULL default '1',
  `created_on` datetime default NULL,
  `type` varchar(20) default NULL,
  `subtype` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

LOCK TABLES `searches` WRITE;
/*!40000 ALTER TABLE `searches` DISABLE KEYS */;

INSERT INTO `searches` (`id`, `query`, `active`, `created_on`, `type`, `subtype`)
VALUES
	(1,'smile',1,NULL,'group1','subgroup1'),
	(2,'asc2012convener',1,NULL,'group2','subgroup1');

/*!40000 ALTER TABLE `searches` ENABLE KEYS */;
UNLOCK TABLES;

# Dump of table searchTermIndex
# ------------------------------------------------------------

DROP TABLE IF EXISTS `searchTermIndex`;

CREATE TABLE `searchTermIndex` (
  `id` int(11) NOT NULL auto_increment,
  `searchId` int(11) default NULL,
  `date` date default NULL,
  `tweetId` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `date` (`date`),
  KEY `searchId` (`searchId`),
  KEY `tweetId` (`tweetId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table trackLimitations
# ------------------------------------------------------------

DROP TABLE IF EXISTS `trackLimitations`;

CREATE TABLE `trackLimitations` (
  `id` int(11) NOT NULL auto_increment,
  `numberLimited` int(11) default NULL,
  `lastTweetDateBeforeLimit` datetime default NULL,
  `lastTweetIdBeforeLimit` bigint(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table tweets
# ------------------------------------------------------------

DROP TABLE IF EXISTS `tweets`;

CREATE TABLE `tweets` (
  `id` bigint(20) NOT NULL,
  `source` varchar(300) default NULL,
  `text` varbinary(450) default NULL,
  `createdAt` varchar(64) default NULL,
  `created_at_GMT` datetime default NULL,
  `to_user_id` bigint(20) default NULL,
  `to_user_id_Search` bigint(11) default NULL,
  `to_user` varchar(32) default NULL,
  `from_user_id` bigint(20) default NULL,
  `from_user_id_Search` bigint(11) default NULL,
  `from_user` varchar(32) default NULL,
  `hasGeoCode` tinyint(1) default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `isTruncated` tinyint(1) default NULL,
  `inReplyToStatusId` bigint(20) default NULL,
  `retweetedStatus` tinyint(1) default NULL,
  `retweetedId` bigint(20) default NULL,
  `contributors` varchar(300) default NULL,
  `place` varchar(700) default NULL,
  `isFavorited` tinyint(1) default NULL,
  `sourceAPI` varchar(20) default NULL,
  `record_add_date` datetime default NULL,
  PRIMARY KEY  (`id`),
  KEY `from_user` (`from_user`),
  KEY `from_user_id` (`from_user_id`),
  KEY `from_user_id_Search` (`from_user_id_Search`),
  KEY `to_user_id` (`to_user_id`),
  KEY `to_user_id_Search` (`to_user_id_Search`),
  KEY `to_user` (`to_user`),
  KEY `created_at_GMT` (`created_at_GMT`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL default '0',
  `name` varbinary(100) default NULL,
  `screenName` varchar(32) default NULL,
  `location` varbinary(110) default NULL,
  `description` varbinary(500) default NULL,
  `profileImageUrl` varchar(320) default NULL,
  `url` varchar(255) default NULL,
  `isProtected` tinyint(1) default NULL,
  `followersCount` int(11) default NULL,
  `status` varchar(200) default NULL,
  `profileBackgroundColor` varchar(6) default NULL,
  `profileTextColor` varchar(6) default NULL,
  `profileLinkColor` varchar(6) default NULL,
  `profileSidebarFillColor` varchar(6) default NULL,
  `profileSidebarBorderColor` varchar(6) default NULL,
  `friendsCount` int(11) default NULL,
  `created_at_GMT` datetime default NULL,
  `favouritesCount` int(11) default NULL,
  `utcOffset` int(11) default NULL,
  `timeZone` varchar(40) default NULL,
  `profileBackgroundImageUrl` varchar(315) default NULL,
  `profileBackgroundTile` varchar(255) default NULL,
  `statusesCount` bigint(20) default NULL,
  `geoEnabled` tinyint(1) default NULL,
  `verified` tinyint(1) default NULL,
  `listedCount` int(11) default NULL,
  `getLang` varchar(100) default NULL,
  `contributorsEnabled` tinyint(1) default NULL,
  `useProfileBackgroundImage` tinyint(1) default NULL,
  `showInlineMedia` tinyint(1) default NULL,
  `isTranslator` tinyint(1) default NULL,
  `dateAddedToDatabase` datetime default NULL,
  `sourceAPI` varchar(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `screenName` (`screenName`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table WordList
# ------------------------------------------------------------

DROP TABLE IF EXISTS `WordList`;

CREATE TABLE `WordList` (
  `id` int(11) NOT NULL auto_increment,
  `searchId` int(11) default NULL,
  `date` date default NULL,
  `word` varchar(30) default NULL,
  `frequency` int(11) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `wordKey` (`searchId`,`date`,`word`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table WordListTweetsLookup
# ------------------------------------------------------------

DROP TABLE IF EXISTS `WordListTweetsLookup`;

CREATE TABLE `WordListTweetsLookup` (
  `id` int(11) NOT NULL auto_increment,
  `wordListId` int(11) default NULL,
  `tweetId` bigint(20) default NULL,
  PRIMARY KEY  (`id`),
  KEY `wordListId` (`wordListId`),
  KEY `tweetId` (`tweetId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
