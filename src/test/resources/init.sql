create database  gsns default  charset 'utf8';
create database  gsns_test default  charset 'utf8';
create database  gsns_dev default  charset 'utf8';

CREATE TABLE gsns.user(id int not null primary key auto_increment ,name char(20));
CREATE TABLE gsns_dev.user(id int not null primary key auto_increment ,name char(20));
CREATE TABLE gsns_test.user(id int not null primary key auto_increment ,name char(20));

CREATE TABLE gsns.friend(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_test.friend(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_dev.friend(id int not null primary key,name char(20),weight int,height int);

CREATE TABLE gsns.friend_shard(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_test.friend_shard(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_dev.friend_shard(id int not null primary key,name char(20),weight int,height int);

CREATE TABLE gsns.friend_shard_0(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_test.friend_shard_0(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_dev.friend_shard_0(id int not null primary key,name char(20),weight int,height int);

CREATE TABLE gsns.friend_shard_1(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_test.friend_shard_1(id int not null primary key,name char(20),weight int,height int);
CREATE TABLE gsns_dev.friend_shard_1(id int not null primary key,name char(20),weight int,height int);


