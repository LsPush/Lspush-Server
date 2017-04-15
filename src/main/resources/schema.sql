-- CREATE SCHEMA `lspush2` DEFAULT CHARACTER SET utf8 ;

drop table if exists favor;
drop table if exists comment;
drop table if exists collection;
drop table if exists follow;
drop table if exists user;

create table user (
    id bigint auto_increment not null,
    username varchar(36),
    phone varchar(24), -- 允许多个账号绑定到相同的手机号
    country varchar(2), -- iso country
    password varchar(24), -- ([a-zA-Z0-9\\.,;]){6,}
    avatar varchar(128), -- <del>file unique code in service</del> 完整网址
    description varchar(128), -- 用户描述字段
    primary key(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table collection (
	  id bigint auto_increment not null,
	  url text(1024) not null,
    title varchar(200),
    description varchar(512),
    image text(1024),
    tags varchar(512),
    user_id bigint not null,
    create_date timestamp default CURRENT_TIMESTAMP,
    update_date timestamp default CURRENT_TIMESTAMP,
    primary key(`id`),
    KEY `user_id` (`user_id`),
	  CONSTRAINT `collection_user_id_constraint` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table favor (
    id bigint auto_increment not null,
    user_id bigint not null,
    col_id bigint not null,
    update_date timestamp default CURRENT_TIMESTAMP,
    primary key(`id`),
    KEY `user_id` (`user_id`),
	  CONSTRAINT `favor_user_id_constraint` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
	  KEY `col_id` (`col_id`),
	  CONSTRAINT `favor_col_id_constraint` FOREIGN KEY (`col_id`) REFERENCES `collection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table comment (
    id bigint auto_increment not null,
    user_id bigint not null,
    col_id bigint not null,
    update_date timestamp default CURRENT_TIMESTAMP,
    comment varchar(512),
    primary key(`id`),
    KEY `user_id` (`user_id`),
	  CONSTRAINT `comment_user_id_constraint` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
	  KEY `col_id` (`col_id`),
	  CONSTRAINT `comment_col_id_constraint` FOREIGN KEY (`col_id`) REFERENCES `collection` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table follow (
    id bigint auto_increment not null,
    owner_id bigint not null,
    following_id bigint not null,
    update_date timestamp default CURRENT_TIMESTAMP,
    primary key(`id`),
    KEY `owner_id` (`owner_id`),
	  CONSTRAINT `follow_owner_id_constraint` FOREIGN KEY (`owner_id`) REFERENCES `user` (`id`),
	  KEY `following_id` (`following_id`),
	  CONSTRAINT `follow_following_id_constraint` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- create table pin (
--     id bigint auto_increment NOT NULL,
--     user_id VARCHAR(24) NOT NULL,
--     pins VARCHAR(512),
--     pin_date TIMESTAMP,
--     PRIMARY KEY (`id`),
--     KEY `user_id` (`user_id`),
--     CONSTRAINT `pin_user_id_constraint` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
-- )  ENGINE=INNODB DEFAULT CHARSET=UTF8;