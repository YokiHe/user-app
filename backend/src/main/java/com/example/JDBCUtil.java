package com.example;

import com.alibaba.druid.pool.DruidDataSource;

public class JDBCUtil {
    private static final DruidDataSource DS = new DruidDataSource();

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL 驱动未找到，检查 pom.xml", e);
        }
        // 优先读环境变量（Render/Aiven 云端部署用），没有则回落到本地课堂默认值
        // DB_URL 示例（Aiven MySQL，必须带 sslMode=REQUIRED）：
        // jdbc:mysql://<主机>:<端口>/defaultdb?sslMode=REQUIRED&serverTimezone=Asia/Shanghai
        DS.setUrl(System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/userdb?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"));
        DS.setUsername(System.getenv().getOrDefault("DB_USER", "root"));
        DS.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "root")); // 课堂统一口令；机房密码不同以黑板为准
        DS.setInitialSize(5);
        DS.setMaxActive(10);
    }

    public static DruidDataSource getDataSource() {
        return DS;
    }
}
