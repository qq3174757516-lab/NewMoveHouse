# 搬家预约系统

包含一个统一后端和三个独立 Vite 前端：

- `movehouse-server`：Spring Boot 2.7 + MyBatis + MySQL 8 + Redis + WebSocket。
- `user-app`：用户端，发布订单、支付、评价、查看司机位置。
- `driver-app`：司机端，接单大厅、抢单、状态流转、位置上报。
- `admin-app`：管理后台，看板、用户/司机/订单/车型/计价/评价管理。

## 初始化数据库

```bash
mysql -uroot -p < sql/schema.sql
mysql -uroot -p < sql/init-data.sql
```

默认管理员：

- 用户名：`admin`
- 密码：`admin123`

## 后端启动

1. 复制配置：

```bash
copy movehouse-server\src\main\resources\application.yml.example movehouse-server\src\main\resources\application.yml
```

2. 修改 `application.yml` 中的 MySQL、Redis、高德地图服务端 Key。
3. 启动 Redis。
4. 启动后端：

```bash
cd movehouse-server
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`，WebSocket 地址：`ws://localhost:8080/ws?token=JWT`。

## 前端启动

三个前端都需要复制 `.env.example` 为 `.env`，并填入高德 Web Key。
如果你的高德 Web Key 是 2021 年 12 月之后申请的，还需要填写 `VITE_AMAP_SECURITY_JS_CODE`。

用户端：

```bash
cd user-app
npm install
npm run dev
```

司机端：

```bash
cd driver-app
npm install
npm run dev
```

管理端：

```bash
cd admin-app
npm install
npm run dev
```

默认开发端口：

- 用户端：`http://localhost:5173`
- 司机端：`http://localhost:5174`
- 管理端：`http://localhost:5175`

## 核心流程

1. 管理员登录后台，审核司机注册申请为 `APPROVED`。
2. 用户端注册登录，选择车型、起点、终点、楼层、电梯、大件数量和预约时间。
3. 用户调用估价并提交订单，后端根据车型、驾车距离、楼层费、大件费、夜间费生成最终费用。
4. 后端通过 WebSocket 向司机端推送 `NEW_ORDER`。
5. 审核通过的司机在接单大厅抢单。
6. 司机按顺序操作：到达装货地、开始搬运、搬运完成。
7. 用户端收到状态变更推送，订单到 `MOVED` 后模拟支付。
8. 支付后订单变为 `COMPLETED`，用户可评价司机。

## 计价规则

车型表维护起步价和每公里价格。后台计价规则表维护：

- `floor_fee_per_floor`：无电梯每层加价。
- `large_item_fee`：每件大件物品附加费。
- `night_service_fee`：夜间服务费，默认 22:00-06:00。

历史订单保存下单时计算出的 `estimated_amount` 和 `final_amount`，后台规则调整不会影响历史订单。

## 升级已有数据库

如果你已经运行过旧版 `schema.sql`，本次优化新增了车型排序号和支付方式字段，请执行：

```bash
mysql -uroot -p < sql/upgrade-20260502-payment-vehicle-audit.sql
```

## 部署

1. 三个前端分别执行 `npm run build`。
2. 将构建产物分别放到：
   - `/var/www/movehouse/user-app`
   - `/var/www/movehouse/driver-app`
   - `/var/www/movehouse/admin-app`
3. 使用 `deploy/nginx.conf.example` 作为 Nginx 配置参考。

## 说明

- 图片上传凭证当前为扩展点，搬运完成暂不强制上传图片。
- 未配置高德 Key 时，后端会用直线距离乘以 1.25 作为本地演示兜底估算。
- 司机位置存入 Redis，Key 为 `driver:location:{driverId}`，5 分钟过期。
