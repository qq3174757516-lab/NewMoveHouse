use new_move_house;

-- 系统公告：面向用户/司机隔离
create table if not exists announcements (
  id bigint primary key auto_increment,
  audience varchar(16) not null comment 'USER or DRIVER',
  title varchar(128) not null,
  content varchar(2000) not null,
  enabled tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_audience_enabled(audience, enabled)
) engine=InnoDB default charset=utf8mb4;

-- 司机资质材料（保险/驾驶证等）
create table if not exists driver_documents (
  id bigint primary key auto_increment,
  driver_id bigint not null,
  doc_type varchar(32) not null comment 'INSURANCE or LICENSE',
  file_url varchar(512) not null,
  original_name varchar(255),
  created_at datetime not null default current_timestamp,
  index idx_driver_doc(driver_id, doc_type),
  constraint fk_doc_driver foreign key(driver_id) references drivers(id)
) engine=InnoDB default charset=utf8mb4;
