use new_move_house;

insert into admins(username,password_hash,real_name)
values('admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','系统管理员');

insert into vehicle_types(name,description,base_price,per_km_price,load_capacity,sort_order,enabled) values
('小面包','适合少量纸箱和小件家具',88.00,4.00,0.80,10,1),
('金杯','适合一居室搬家',128.00,5.50,1.20,20,1),
('4.2米厢货','适合整屋搬迁和大件家具',268.00,8.00,3.00,30,1);

insert into pricing_rules(rule_key,rule_value,description,enabled) values
('floor_fee_per_floor',10.00,'无电梯时每层加价',1),
('large_item_fee',30.00,'每件大件物品附加费',1),
('night_service_fee',50.00,'22:00-06:00 夜间服务费',1);
