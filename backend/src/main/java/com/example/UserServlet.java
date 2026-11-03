package com.example;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 用户列表（后端分页+搜索）/ 新增 / 删除（PUT 编辑留给课后综合任务） */
@WebServlet("/api/v1/users/*")
public class UserServlet extends HttpServlet {

    private final UserDaoImpl userDao = new UserDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        int page = parseIntOr(request.getParameter("page"), 1);
        int size = parseIntOr(request.getParameter("size"), 5);
        String username = request.getParameter("username");

        List<User> list = userDao.findByPage(page, size, username);
        int total = userDao.countAll(username);
        for (User u : list) u.setPassword(null);   // 密码不回传前端

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        response.getWriter().println(gson.toJson(Result.ok(data)));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        User form = gson.fromJson(request.getReader(), User.class);
        if (form == null || isBlank(form.getUsername()) || isBlank(form.getPassword())) {
            response.getWriter().println(gson.toJson(new Result(400, "用户名和密码不能为空", null)));
            return;
        }
        if (userDao.findByUsername(form.getUsername().trim()) != null) {
            response.getWriter().println(gson.toJson(new Result(400, "用户名已存在", null)));
            return;
        }
        userDao.insert(form);
        response.getWriter().println(gson.toJson(new Result(201, "新增成功", null)));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            response.getWriter().println(gson.toJson(new Result(400, "必须指定要删除的 ID", null)));
            return;
        }
        try {
            int rows = userDao.deleteById(Integer.parseInt(pathInfo.substring(1)));
            if (rows > 0) response.getWriter().println(gson.toJson(Result.ok("删除成功")));
            else response.getWriter().println(gson.toJson(new Result(404, "用户不存在", null)));
        } catch (NumberFormatException e) {
            response.getWriter().println(gson.toJson(new Result(400, "id 必须是数字", null)));
        }
    }

    private int parseIntOr(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
