# common-web

Web 层通用库：全局异常、统一响应包装、CORS、Jackson 时间处理、TraceId 与简单请求日志。

## 提供内容
- `@NoWrap`：标注在 Controller 类/方法，禁用统一返回包装
- `GlobalResponseAdvice`：将成功结果包成 `ApiResponse`
- `GlobalExceptionHandler`：处理校验/参数/方法不支持/媒体类型/业务异常等
- `CorsProperties` + `WebMvcConfig`：可配置的 CORS + Jackson ISO-8601 时间
- `TraceIdFilter`：注入/回传 `X-Trace-Id` 并写入 MDC
- `RequestLoggingFilter`：简单的请求日志（方法、路径、状态、耗时）

## 使用方式（在 web-api 模块）
1. 依赖本模块：
   ```xml
   <dependency>
     <groupId>com.example</groupId>
     <artifactId>common-web</artifactId>
     <version>${project.version}</version>
   </dependency>
   ```
2. 注册过滤器（任一 @Configuration 类中）：
   ```java
   @Bean
   public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
     FilterRegistrationBean<TraceIdFilter> bean = new FilterRegistrationBean<>(new TraceIdFilter());
     bean.setOrder(1);
     return bean;
   }
   @Bean
   public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilter() {
     FilterRegistrationBean<RequestLoggingFilter> bean = new FilterRegistrationBean<>(new RequestLoggingFilter());
     bean.setOrder(2);
     return bean;
   }
   ```
3. 配置 CORS（application.yml）：
   ```yaml
   app:
     cors:
       allowed-origins: ["http://localhost:5173"]
       allowed-methods: ["GET","POST","PUT","PATCH","DELETE","OPTIONS"]
       allowed-headers: ["*"]
       exposed-headers: ["X-Trace-Id"]
       allow-credentials: true
       max-age: 3600
   ```
4. 若某接口不想被统一包装，标注 `@NoWrap`。