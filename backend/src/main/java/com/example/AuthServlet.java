package com.example;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/** 登录 / 当前登录人 / 退出 三合一（通配路由 + getPathInfo 分流） */
@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserDaoImpl userDao = new UserDaoImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        String path = request.getPathInfo();   // "/login" 或 "/logout"

        if ("/login".equals(path)) {
            // ① 接参数：Gson 把 JSON 请求体转 User（只取 username/password）
            User form = gson.fromJson(request.getReader(), User.class);
            if (form == null || form.getUsername() == null || form.getPassword() == null) {
                response.getWriter().println(gson.toJson(new Result(400, "用户名和密码不能为空", null)));
                return;
            }
            // ② 查库比对（课堂口径：明文比对；真实项目要加盐哈希——第 11 周综合项目做）
            User user = userDao.findByUsername(form.getUsername().trim());
            if (user == null || !user.getPassword().equals(form.getPassword())) {
                response.getWriter().println(gson.toJson(new Result(400, "用户名或密码错误", null)));
                return;
            }
            // ③ 发通行证：把 user 装进 Session——Filter 就认它（key = "loginUser"，两边必须逐字一致）
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", user);
            user.setPassword(null);   // 不把密码回传给前端
            response.getWriter().println(gson.toJson(Result.ok(user)));
            return;
        }

        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();   // 销毁通行证
            response.getWriter().println(gson.toJson(Result.ok(null)));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // GET /api/v1/auth/me：当前登录人是谁
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession(false);
        User user = (session == null) ? null : (User) session.getAttribute("loginUser");
        if (user == null) {
            response.getWriter().println(gson.toJson(new Result(401, "未登录", null)));
        } else {
            response.getWriter().println(gson.toJson(Result.ok(user)));
        }
    }
}
