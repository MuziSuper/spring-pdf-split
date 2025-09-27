/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : pdf_split

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 27/09/2025 11:49:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pdf_info
-- ----------------------------
DROP TABLE IF EXISTS `pdf_info`;
CREATE TABLE `pdf_info` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `file_name` varchar(255) DEFAULT NULL,
                            `gmt_created` datetime(6) DEFAULT NULL,
                            `gmt_modified` datetime(6) DEFAULT NULL,
                            `key` varchar(255) DEFAULT NULL,
                            `md5` varchar(255) DEFAULT NULL,
                            `status` tinyint DEFAULT NULL,
                            `url` varchar(255) DEFAULT NULL,
                            `user_id` bigint DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            CONSTRAINT `pdf_info_chk_1` CHECK ((`status` between 0 and 2))
) ENGINE=InnoDB AUTO_INCREMENT=1971217324588720130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
