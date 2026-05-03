use new_move_house;

insert into admins(username,password_hash,real_name)
values('admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','系统管理员');

insert into vehicle_types(id,name,description,base_price,per_km_price,load_capacity,sort_order,enabled) values
(1,'小面包车','车厢尺寸：约1.6-1.8米长 × 1.2米宽 × 1.1米高。适用场景：小件物品、行李箱、小型家电，适合单人搬家或短途配送',98.00,4.50,0.80,1,1),
(2,'中面包车','车厢尺寸：约2.4米长 × 1.3-1.4米宽 × 1.2米高。适用场景：中型家电、家具、小型建材，适合家庭搬家或小商户配送',138.00,5.80,1.20,2,1),
(3,'依维柯/小型厢货','车厢尺寸：约3-3.5米长 × 1.8米宽 × 1.8米高。适用场景：大型家具、家电、批量货物',198.00,7.00,2.50,3,1),
(4,'中型厢货','车厢尺寸：约4.2米长 × 2米宽 × 2米高。适用场景：大批量货物、建材、设备运输',268.00,8.50,4.00,4,1),
(5,'大型厢货/平板车','车厢尺寸：约6.2米长 × 2.3米宽 × 2.3米高（平板车无车厢）。适用场景：超大批量货物、机械设备、建材',368.00,10.00,8.00,5,1);

insert into pricing_rules(rule_key,rule_value,description,enabled) values
('floor_fee_per_floor',10.00,'无电梯时每层加价',1),
('large_item_fee',30.00,'每件大件物品附加费',1),
('night_service_fee',50.00,'22:00-06:00 夜间服务费',1);
