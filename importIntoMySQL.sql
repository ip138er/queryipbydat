#create table
CREATE TABLE `ips` (
  `start` int(11) unsigned NOT NULL,
  `end` int(11) unsigned NOT NULL,
  `startip` varchar(64) NOT NULL,
  `endip` varchar(64) NOT NULL,
  `country` varchar(64) NOT NULL,
  `province` varchar(64) NOT NULL,
  `city` varchar(64) NOT NULL,
  `operator` varchar(64) NOT NULL,
  `zipcode` int(11) unsigned NOT NULL,
  `areacode` int(11) unsigned NOT NULL,
  UNIQUE KEY `index` (`start`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#import into table ips from file ip_20191011091025.txt
load data infile 'ip_20191011091025.txt' ignore into table ips character set utf8 fields terminated by ',' enclosed by '' lines terminated by '\n' (`start`,`end`,`startip`,`endip`,`country`,`province`,`city`,`operator`,`zipcode`,`areacode`);

#ip location
select * from ips WHERE start<=3748151430 order by start desc limit 1;