create database if not exists new_move_house default charset utf8mb4 collate utf8mb4_unicode_ci;
use new_move_house;

drop table if exists admin_operation_logs;
drop table if exists driver_audit_records;
drop table if exists reviews;
drop table if exists payment_records;
drop table if exists order_status_logs;
drop table if exists move_orders;
drop table if exists user_addresses;
drop table if exists pricing_rules;
drop table if exists vehicle_types;
drop table if exists drivers;
drop table if exists admins;
drop table if exists users;

create table users (
  id bigint primary key auto_increment,
  username varchar(64) not null unique,
  password_hash varchar(128) not null,
  phone varchar(32) not null,
  nickname varchar(64),
  status tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4;

create table admins (
  id bigint primary key auto_increment,
  username varchar(64) not null unique,
  password_hash varchar(128) not null,
  real_name varchar(64),
  created_at datetime not null default current_timestamp
) engine=InnoDB default charset=utf8mb4;

create table vehicle_types (
  id bigint primary key auto_increment,
  name varchar(64) not null,
  description varchar(2048),
  base_price decimal(10,2) not null,
  per_km_price decimal(10,2) not null,
  load_capacity decimal(10,2),
  sort_order int not null default 0,
  enabled tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4;

create table drivers (
  id bigint primary key auto_increment,
  username varchar(64) not null unique,
  password_hash varchar(128) not null,
  phone varchar(32) not null,
  real_name varchar(64) not null,
  vehicle_plate varchar(32) not null,
  vehicle_type_id bigint not null,
  service_area varchar(128),
  audit_status varchar(32) not null default 'PENDING',
  status tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_driver_vehicle foreign key(vehicle_type_id) references vehicle_types(id)
) engine=InnoDB default charset=utf8mb4;

create table pricing_rules (
  id bigint primary key auto_increment,
  rule_key varchar(64) not null unique,
  rule_value decimal(10,2) not null,
  description varchar(255),
  enabled tinyint not null default 1,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp
) engine=InnoDB default charset=utf8mb4;

create table user_addresses (
  id bigint primary key auto_increment,
  user_id bigint not null,
  name varchar(64) not null,
  detail varchar(255) not null,
  lng decimal(12,6) not null,
  lat decimal(12,6) not null,
  contact_name varchar(64),
  contact_phone varchar(32),
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_addr_user foreign key(user_id) references users(id)
) engine=InnoDB default charset=utf8mb4;

create table move_orders (
  id bigint primary key auto_increment,
  user_id bigint not null,
  driver_id bigint,
  vehicle_type_id bigint not null,
  start_address varchar(255) not null,
  start_lng decimal(12,6) not null,
  start_lat decimal(12,6) not null,
  end_address varchar(255) not null,
  end_lng decimal(12,6) not null,
  end_lat decimal(12,6) not null,
  item_description varchar(500),
  start_has_elevator tinyint default 1,
  end_has_elevator tinyint default 1,
  start_floor int default 1,
  end_floor int default 1,
  large_item_count int default 0,
  appointment_time datetime,
  contact_name varchar(64) not null,
  contact_phone varchar(32) not null,
  distance_km decimal(10,2) not null,
  estimated_amount decimal(10,2) not null,
  final_amount decimal(10,2) not null,
  status varchar(32) not null,
  payment_status varchar(32) not null,
  cancel_reason varchar(255),
  accepted_at datetime,
  arrived_at datetime,
  moving_at datetime,
  moved_at datetime,
  completed_at datetime,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  index idx_order_user(user_id),
  index idx_order_driver(driver_id),
  index idx_order_status(status),
  constraint fk_order_user foreign key(user_id) references users(id),
  constraint fk_order_driver foreign key(driver_id) references drivers(id),
  constraint fk_order_vehicle foreign key(vehicle_type_id) references vehicle_types(id)
) engine=InnoDB default charset=utf8mb4;

create table order_status_logs (
  id bigint primary key auto_increment,
  order_id bigint not null,
  operator_role varchar(32) not null,
  operator_id bigint not null,
  from_status varchar(32),
  to_status varchar(32) not null,
  remark varchar(255),
  created_at datetime not null default current_timestamp,
  constraint fk_log_order foreign key(order_id) references move_orders(id)
) engine=InnoDB default charset=utf8mb4;

create table payment_records (
  id bigint primary key auto_increment,
  order_id bigint not null,
  user_id bigint not null,
  amount decimal(10,2) not null,
  pay_status varchar(32) not null,
  pay_channel varchar(32) not null,
  payment_method varchar(32) not null default 'WECHAT_PAY',
  transaction_no varchar(64) not null,
  paid_at datetime,
  created_at datetime not null default current_timestamp,
  constraint fk_pay_order foreign key(order_id) references move_orders(id)
) engine=InnoDB default charset=utf8mb4;

create table reviews (
  id bigint primary key auto_increment,
  order_id bigint not null unique,
  user_id bigint not null,
  driver_id bigint not null,
  rating int not null,
  content varchar(500),
  hidden tinyint not null default 0,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  constraint fk_review_order foreign key(order_id) references move_orders(id)
) engine=InnoDB default charset=utf8mb4;

create table driver_audit_records (
  id bigint primary key auto_increment,
  driver_id bigint not null,
  admin_id bigint not null,
  audit_status varchar(32) not null,
  reason varchar(255),
  created_at datetime not null default current_timestamp,
  constraint fk_audit_driver foreign key(driver_id) references drivers(id),
  constraint fk_audit_admin foreign key(admin_id) references admins(id)
) engine=InnoDB default charset=utf8mb4;

create table admin_operation_logs (
  id bigint primary key auto_increment,
  admin_id bigint not null,
  action varchar(64) not null,
  target_type varchar(64),
  target_id bigint,
  detail varchar(500),
  created_at datetime not null default current_timestamp,
  constraint fk_op_admin foreign key(admin_id) references admins(id)
) engine=InnoDB default charset=utf8mb4;
