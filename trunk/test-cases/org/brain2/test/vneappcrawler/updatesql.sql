ALTER TABLE `topic_article`  CHANGE COLUMN `creation_time` `creation_time` BIGINT(20) UNSIGNED NULL DEFAULT NULL AFTER `status`,  CHANGE COLUMN `update_time` `update_time` BIGINT(20) UNSIGNED NULL DEFAULT NULL AFTER `creation_time`;
insert into topic_detail(topic_id,title) select ID,Name from topic;s
/*
 * random between 1 and 224
 */
update article set article.author_id = ROUND(1+(RAND()* 223),0);
delete from `comment`;
delete from object_reference;
delete from topic_article;
delete from article;

insert into author(author_name) select DISTINCT PostBy from subject0