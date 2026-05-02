use new_move_house;

alter table complaints
  add column image_url varchar(512) not null default '' after content,
  add column action_type varchar(32) not null default 'NONE' after status,
  add column penalty_amount decimal(10,2) not null default 0 after action_type;

