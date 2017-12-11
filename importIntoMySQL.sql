#create table
CREATE TABLE `ips` (
`start` int(11) NOT NULL,
`end` int(11) NOT NULL,
`startip` varchar(64) NOT NULL,
`endip` varchar(64) NOT NULL,
`country` varchar(64) NOT NULL,
`province` varchar(64) NOT NULL,
`city` varchar(64) NOT NULL,
`operator` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#import into table ips from file ip_20171211091025.txt
load data infile 'ip_20171211091025.txt' ignore into table ips character set utf8 fields terminated by ',' enclosed by '' lines terminated by '\n' (`start`,`end`,`startip`,`endip`,`country`,`province`,`city`,`operator`);
