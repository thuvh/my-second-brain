ALTER TABLE `topic_article`  CHANGE COLUMN `creation_time` `creation_time` BIGINT(20) UNSIGNED NULL DEFAULT NULL AFTER `status`,  CHANGE COLUMN `update_time` `update_time` BIGINT(20) UNSIGNED NULL DEFAULT NULL AFTER `creation_time`;
insert into topic_detail(topic_id,title) select ID,Name from topic;