# ************************************************************
# Sequel Pro SQL dump
# Version 3408
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: 127.0.0.1 (MySQL 5.1.63-0+squeeze1-log)
# Database: twitterArchive
# Generation Time: 2012-07-24 07:04:59 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table archive
# ------------------------------------------------------------

DROP TABLE IF EXISTS `archive`;

CREATE TABLE `archive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `search_id` int(11) NOT NULL,
  `tweet_id` bigint(20) NOT NULL,
  `record_add_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SEARCHES_ARCHIVE` (`search_id`),
  KEY `FK_TWEETS_ARCHIVE` (`tweet_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;



# Dump of table searches
# ------------------------------------------------------------

DROP TABLE IF EXISTS `searches`;

CREATE TABLE `searches` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `query` varchar(140) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `lastFoundCount` int(4) DEFAULT NULL,
  `lastSearchDate` datetime DEFAULT NULL,
  `created_on` datetime DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `subtype` varchar(20) DEFAULT NULL,
  `test` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table tweets
# ------------------------------------------------------------

DROP TABLE IF EXISTS `tweets`;

CREATE TABLE `tweets` (
  `id` bigint(20) NOT NULL,
  `iso_language_code` char(3) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `text` varchar(200) DEFAULT NULL,
  `created_at` varchar(64) DEFAULT NULL,
  `to_user_id` bigint(20) DEFAULT NULL,
  `to_user` varchar(32) DEFAULT NULL,
  `from_user_id` bigint(20) DEFAULT NULL,
  `from_user` varchar(32) DEFAULT NULL,
  `hasGeoCode` int(1) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `record_add_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
