package com.example;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class UserDaoImpl {

    private final QueryRunner runner = new QueryRunner(JDBCUtil.getDataSource());

    /** 按用户名查用户（登录/重名校验用），查不到返回 null */
    public User findByUsername(String username) {
        try {
            return runner.query(
                    "SELECT id, username, password, nickname FROM tb_user WHERE username = ?",
                    new BeanHandler<>(User.class), username);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** 分页 + 按用户名搜索（后端分页：LIMIT 取 size 条，OFFSET 跳过 (page-1)*size 条） */
    public List<User> findByPage(int page, int size, String username) {
        try {
            String sql = "SELECT id, username, password, nickname FROM tb_user";
            boolean hasKeyword = username != null && !username.isEmpty();
            if (hasKeyword) sql += " WHERE username LIKE ?";
            sql += " ORDER BY id LIMIT ? OFFSET ?";
            return hasKeyword
                    ? runner.query(sql, new BeanListHandler<>(User.class),
                        "%" + username + "%", size, (page - 1) * size)
                    : runner.query(sql, new BeanListHandler<>(User.class), size, (page - 1) * size);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** 用户总数（分页 total 用） */
    public int countAll(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM tb_user";
            boolean hasKeyword = username != null && !username.isEmpty();
            if (hasKeyword) sql += " WHERE username LIKE ?";
            Long n = hasKeyword
                    ? runner.query(sql, new ScalarHandler<Long>(), "%" + username + "%")
                    : runner.query(sql, new ScalarHandler<Long>());
            return n.intValue();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** 新增用户 */
    public void insert(User u) {
        try {
            runner.update("INSERT INTO tb_user (username, password, nickname) VALUES (?, ?, ?)",
                    u.getUsername(), u.getPassword(), u.getNickname());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** 删除用户，返回受影响行数 */
    public int deleteById(int id) {
        try {
            return runner.update("DELETE FROM tb_user WHERE id = ?", id);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    /** 编辑用户（课后综合任务：附录 B），返回受影响行数 */
    public int update(int id, User u) {
        try {
            return runner.update("UPDATE tb_user SET nickname = ?, password = ? WHERE id = ?",
                    u.getNickname(), u.getPassword(), id);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
