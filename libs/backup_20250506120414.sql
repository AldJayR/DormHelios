-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: dormhelios_db
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `emergency_contacts`
--

DROP TABLE IF EXISTS `emergency_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emergency_contacts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `relationship` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emergency_contacts`
--

LOCK TABLES `emergency_contacts` WRITE;
/*!40000 ALTER TABLE `emergency_contacts` DISABLE KEYS */;
/*!40000 ALTER TABLE `emergency_contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `guardians`
--

DROP TABLE IF EXISTS `guardians`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `guardians` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guardians`
--

LOCK TABLES `guardians` WRITE;
/*!40000 ALTER TABLE `guardians` DISABLE KEYS */;
/*!40000 ALTER TABLE `guardians` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `tenant_id` int NOT NULL,
  `user_id` int NOT NULL,
  `payment_date` date NOT NULL,
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `payment_method` varchar(20) NOT NULL,
  `period_covered_start` date DEFAULT NULL,
  `period_covered_end` date DEFAULT NULL,
  `receipt_reference` varchar(100) DEFAULT NULL,
  `qr_code_data` text,
  `notes` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  KEY `tenant_id` (`tenant_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`),
  CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `payments_chk_1` CHECK ((`payment_method` in (_utf8mb4'CASH',_utf8mb4'BANK_TRANSFER',_utf8mb4'GCASH',_utf8mb4'MAYA',_utf8mb4'OTHER')))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,1,5,'2025-02-25',1500.00,'CASH','2025-02-01','2025-02-28',NULL,'Tenant:Justineh Quilantang\nRoom:103\nDate:2025-02-25\nAmount:1500.00\nPeriod:Feb 2025',NULL,'2025-05-05 07:54:04'),(2,3,5,'2025-03-25',1500.00,'CASH','2025-03-01','2025-03-31',NULL,'Tenant:12 1212\nRoom:104\nDate:2025-03-25\nAmount:1500.00\nPeriod:Mar 2025',NULL,'2025-05-05 08:17:12'),(3,1,5,'2025-05-05',1500.00,'CASH','2025-05-01','2025-05-31',NULL,'Tenant:Justineh Quilantang\nRoom:104\nDate:2025-05-05\nAmount:1500.00\nPeriod:May 2025',NULL,'2025-05-05 11:59:39'),(4,10,5,'2025-05-06',1500.00,'CASH','2025-05-01','2025-05-31',NULL,'Tenant:Amiel Ardee aclan\nRoom:104\nDate:2025-05-06\nAmount:1500.00\nPeriod:May 2025',NULL,'2025-05-05 16:35:05'),(5,22,5,'2025-05-06',1500.00,'CASH','2025-05-01','2025-05-31',NULL,'Tenant:Aldwin Jairo Sarte\nRoom:106\nDate:2025-05-06\nAmount:1500.00\nPeriod:May 2025',NULL,'2025-05-06 01:20:46');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `rooms` (
  `id` int NOT NULL AUTO_INCREMENT,
  `room_number` varchar(20) NOT NULL,
  `capacity` int NOT NULL DEFAULT '1',
  `slots_available` int DEFAULT '1',
  `monthly_rate` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` varchar(20) NOT NULL DEFAULT 'VACANT',
  `description` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `room_number` (`room_number`),
  CONSTRAINT `rooms_chk_1` CHECK ((`status` in (_utf8mb4'VACANT',_utf8mb4'OCCUPIED',_utf8mb4'UNDER_MAINTENANCE')))
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,'103',1,0,1500.00,'OCCUPIED',NULL,1,'2025-05-04 12:28:24','2025-05-05 16:33:24'),(2,'105',1,1,1500.00,'VACANT',NULL,0,'2025-05-04 12:28:33','2025-05-04 12:43:44'),(3,'104',4,0,6000.00,'VACANT',NULL,1,'2025-05-04 12:45:53','2025-05-05 16:34:16'),(7,'106',6,4,9000.00,'VACANT',NULL,1,'2025-05-05 12:51:01','2025-05-06 01:18:57');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_logs`
--

DROP TABLE IF EXISTS `system_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_logs` (
  `name` varchar(255) DEFAULT NULL,
  `value` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_logs`
--

LOCK TABLES `system_logs` WRITE;
/*!40000 ALTER TABLE `system_logs` DISABLE KEYS */;
INSERT INTO `system_logs` VALUES ('20250506113007:AdminDashboardPanel','Viewed system logs'),('20250506113013:AdminDashboardPanel','Opened Create User Account'),('20250506113029:AdminDashboardPanel','Printed system logs'),('20250506113031:AdminDashboardPanel','Backup exception: Cannot run program \"mysqldump\": CreateProcess error=2, The system cannot find the file specified'),('20250506113112:AdminDashboardPanel','Backup exception: Cannot run program \"mysqldump\": CreateProcess error=2, The system cannot find the file specified'),('20250506113304:AdminDashboardPanel','Backup exception: Cannot run program \"mysqldump\": CreateProcess error=2, The system cannot find the file specified'),('20250506113308:AdminDashboardPanel','Viewed system logs'),('20250506113413:AdminCreateAccountDialog','Created user email@gmail.com'),('20250506113422:AdminDashboardPanel','Opened Create User Account'),('20250506113552:AdminDashboardPanel','Opened Create User Account'),('20250506113555:AdminDashboardPanel','Opened Create User Account'),('20250506113558:AdminDashboardPanel','Opened Create User Account'),('20250506115410:AdminDashboardPanel','Backup exception: Cannot run program \"mysqldump\": CreateProcess error=2, The system cannot find the file specified');
/*!40000 ALTER TABLE `system_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tenants`
--

DROP TABLE IF EXISTS `tenants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tenants` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `room_id` int DEFAULT NULL,
  `guardian_id` int DEFAULT NULL,
  `emergency_contact_id` int DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `student_number` varchar(50) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `permanent_address` text,
  `lease_start_date` date DEFAULT NULL,
  `lease_end_date` date DEFAULT NULL,
  `deposit_amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `deposit_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `notes` text,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenants_room` (`room_id`),
  KEY `idx_tenants_user` (`user_id`),
  KEY `idx_tenants_guardian` (`guardian_id`),
  KEY `idx_tenants_emergency` (`emergency_contact_id`),
  KEY `idx_tenants_name` (`last_name`,`first_name`),
  CONSTRAINT `tenants_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `tenants_ibfk_2` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`),
  CONSTRAINT `tenants_ibfk_3` FOREIGN KEY (`guardian_id`) REFERENCES `guardians` (`id`),
  CONSTRAINT `tenants_ibfk_4` FOREIGN KEY (`emergency_contact_id`) REFERENCES `emergency_contacts` (`id`),
  CONSTRAINT `tenants_chk_1` CHECK ((`deposit_status` in (_utf8mb4'PAID',_utf8mb4'PENDING',_utf8mb4'REFUNDED',_utf8mb4'PARTIAL_REFUND')))
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tenants`
--

LOCK TABLES `tenants` WRITE;
/*!40000 ALTER TABLE `tenants` DISABLE KEYS */;
INSERT INTO `tenants` VALUES (1,4,3,NULL,NULL,'Justineh','Quilantang','SUM2023-0078','justinequilantang@gmail.com','904024022','sa jaen','2024-02-25','2025-02-26',1500.00,'PAID',NULL,1,'2025-05-04 12:48:45','2025-05-05 11:07:55'),(3,NULL,3,NULL,NULL,'12','1212','323232','23232332@gmail.com','094040234','234234','2024-02-12','2026-10-12',1500.00,'PAID',NULL,1,'2025-05-04 13:20:56','2025-05-05 15:59:06'),(8,NULL,1,NULL,NULL,'dsda','dadad','asdasda','dsadasd@gmail.com','00392903203','321313','2024-10-25','2025-02-25',5000.00,'PAID',NULL,1,'2025-05-05 16:16:56','2025-05-05 16:22:01'),(10,NULL,3,NULL,NULL,'Amiel Ardee','aclan','2042940','lkmdlkdm@gmail.com','092392939','alkdmdl','2024-10-25','2025-05-25',5000.00,'PAID',NULL,1,'2025-05-05 16:18:28','2025-05-05 16:30:33'),(13,NULL,7,NULL,NULL,'sherilyn','moran','03039039','lamamld@gmail.com','3032242323','lkamdalkm','2025-02-25','2026-02-25',5000.00,'PAID',NULL,1,'2025-05-05 16:21:18','2025-05-05 16:21:18'),(21,NULL,3,NULL,NULL,'ankdandkk','laddmalk','323829','32k13jh1k3n@gmail.com','32039203','akakmadkda','2024-10-25','2025-10-26',2500.00,'PAID',NULL,1,'2025-05-05 16:34:15','2025-05-05 16:34:15'),(22,6,7,NULL,NULL,'Aldwin Jairo','Sarte','SUM2023-00978','aldwinjzs3@gmail.com','09266741603','jaen','2024-10-25','2025-10-25',3000.00,'PAID',NULL,1,'2025-05-06 01:18:56','2025-05-06 01:18:56');
/*!40000 ALTER TABLE `tenants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `surname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `users_chk_1` CHECK ((`role` in (_utf8mb4'ADMIN',_utf8mb4'LANDLORD',_utf8mb4'TENANT')))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,NULL,'$2a$12$CICHT7EvCLFGr03/7uxAUugWRr0.CrLIMr/dy/77mAv.p3ETseazm',NULL,NULL,NULL,'test@gmail.com',NULL,1,'2025-05-04 06:26:40','2025-05-04 06:26:40'),(4,NULL,'$2a$12$YRxq/VMqK00ZXHpr1uhr3OovMprNg1t3h9e3/xOEwlTCdQccaotmi','Justine','Quilantang','TENANT','justinequilantang@gmail.com','0921214113',1,'2025-05-04 12:26:16','2025-05-04 12:26:33'),(5,NULL,'$2a$12$auMzxRaSMd0WOgJgebB2jOBv9dDoB1pL2VjI.FbnGouOMZMglPinO','Landlord','My Name','LANDLORD','landlord@gmail.com','09219020199021',1,'2025-05-04 12:27:05','2025-05-04 12:27:20'),(6,NULL,'$2a$12$mnH8rNP69L5TSqqbxBEAkeg7XADvXAzqDzIzpKLe7PmRZ7YrFy7aq','Aldwin Jairo','Sarte','TENANT','aldwinjzs3@gmail.com','09266741603',1,'2025-05-06 01:01:39','2025-05-06 01:02:03'),(7,'Admin','$2a$12$fp.Tvz9V9xtgyUfPY23SF.ugJPAw3xBs6HXi65m6zgAX2vOSKQir2','test','user','ADMIN','admin','09266741603',1,'2025-05-06 03:09:59','2025-05-06 03:10:33'),(8,NULL,'$2a$12$1tB7XmwNBPTKTKAYsoxtBuJishts8991gKXnsq81tUpVFKr6LoAVu',NULL,NULL,NULL,'email@gmail.com',NULL,1,'2025-05-06 03:34:11','2025-05-06 03:34:11');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-06 12:04:19
