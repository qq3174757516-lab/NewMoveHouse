use new_move_house;

alter table vehicle_types
  add column sort_order int not null default 0 after load_capacity;

alter table payment_records
  add column payment_method varchar(32) not null default 'WECHAT_PAY' after pay_channel;

