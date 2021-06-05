-- MySQL dump 10.13  Distrib 5.1.73, for apple-darwin10.3.0 (i386)
--
-- Host: 127.0.0.1    Database: reactor
-- ------------------------------------------------------
-- Server version	5.5.37

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `suite_result`
--

DROP TABLE IF EXISTS `suite_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `suite_result` (
  `SUITE_RESULT_ID` varchar(255) NOT NULL,
  `SUITE_NAME` varchar(255) DEFAULT NULL,
  `PROJECT_NAME` varchar(255) DEFAULT NULL,
  `JOB_NAME` varchar(255) DEFAULT NULL,
  `JOB_BUILD_NUMBER` int(11) DEFAULT NULL,
  `JOB_BUILD_URL` varchar(255) DEFAULT NULL,
  `EXECUTION_RESULT` varchar(45) DEFAULT NULL,
  `START_TIME` bigint(20) DEFAULT NULL,
  `STOP_TIME` bigint(20) DEFAULT NULL,
  `NUMBER_OF_CASES` int(11) DEFAULT NULL,
  `NUMBER_OF_FAILURE` int(11) DEFAULT NULL,
  `INVISIBLE_ENTRY` tinyint(4) DEFAULT '0',
  `PRODUCT_UNDER_TASK` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SUITE_RESULT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suite_property`
--

DROP TABLE IF EXISTS `suite_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `suite_property` (
  `SUITE_PROPERTY_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SUITE_RESULT_ID` varchar(255) NOT NULL,
  `PROPERTY_NAME` varchar(255) DEFAULT NULL,
  `PROPERTY_VALUE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SUITE_PROPERTY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `task_case`
--

DROP TABLE IF EXISTS `task_case`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `task_case` (
  `TASK_CASE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `SUITE_CLASS` varchar(255) NOT NULL,
  `CASE_CLASS` varchar(255) NOT NULL,
  `CASE_METHOD` varchar(255) NOT NULL,
  `CASE_DATA_INFO` varchar(255) NOT NULL,
  `CASE_DATA` varchar(255) DEFAULT '',
  `CASE_ISSUES` varchar(255) DEFAULT '',
  PRIMARY KEY (`TASK_CASE_ID`)
) ENGINE=InnoDB AUTO_INCREMENT=679 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `case_result`
--

DROP TABLE IF EXISTS `case_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `case_result` (
  `CASE_RESULT_ID` varchar(255) NOT NULL,
  `SUITE_RESULT` varchar(255) DEFAULT NULL,
  `TASK_CASE_ID` int(11) DEFAULT '0',
  `EXECUTION_RESULT` varchar(45) DEFAULT NULL,
  `AUT` varchar(255) DEFAULT NULL,
  `START_TIME` bigint(20) DEFAULT NULL,
  `STOP_TIME` bigint(20) DEFAULT NULL,
  `RETRY` int(11) DEFAULT NULL,
  `CASE_STATION` varchar(255) DEFAULT NULL,
  `LOG_DIR` varchar(255) DEFAULT NULL,
  `EXTERNAL_ID` varchar(255) DEFAULT NULL,
  `CASE_ENV` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`CASE_RESULT_ID`),
  CONSTRAINT `fk_suite_result` FOREIGN KEY (`SUITE_RESULT`) REFERENCES `suite_result` (`SUITE_RESULT_ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_task_case` FOREIGN KEY (`TASK_CASE_ID`) REFERENCES `task_case` (`TASK_CASE_ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Table structure for table `task_result_metric`
--

DROP TABLE IF EXISTS `case_result_metric`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `case_result_metric` (
  `CASE_RESULT_METRIC_ID` int(11) NOT NULL AUTO_INCREMENT,
  `CASE_RESULT_ID` varchar(255) DEFAULT NULL,
  `METRIC_GROUP` varchar(255) DEFAULT NULL,
  `METRIC_NAME` varchar(255) DEFAULT NULL,
  `METRIC_VALUE` double DEFAULT NULL,
  PRIMARY KEY (`CASE_RESULT_METRIC_ID`),
  CONSTRAINT `fk_case_result` FOREIGN KEY (`CASE_RESULT_ID`) REFERENCES `case_result` (`CASE_RESULT_ID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-08-05 17:47:01
