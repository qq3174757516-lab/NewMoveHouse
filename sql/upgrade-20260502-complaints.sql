use new_move_house;

-- 投诉工单：用户提交 -> 司机可见 -> 管理员处理
create table if not exists complaints (
  id bigint primary key auto_increment,
  order_id bigint not null,
  user_id bigint not null,
  driver_id bigint not null,
  title varchar(128) not null,
  content varchar(1000) not null,
  status varchar(32) not null default 'OPEN' comment 'OPEN,IN_PROGRESS,RESOLVED,CLOSED',
  admin_remark varchar(500),
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp on update current_timestamp,
  unique key uk_complaint_order(order_id),
  index idx_complaint_driver_status(driver_id, status),
  index idx_complaint_user(user_id),
  constraint fk_complaint_order foreign key(order_id) references move_orders(id),
  constraint fk_complaint_user foreign key(user_id) references users(id),
  constraint fk_complaint_driver foreign key(driver_id) references drivers(id)
) engine=InnoDB default charset=utf8mb4;
