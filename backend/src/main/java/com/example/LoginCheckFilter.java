package com.example;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录守门：白名单（login/logout）放行 → 其余接口查通行证（Session 的 loginUser）
 * ⚠️ 白名单放行必须写在检查之前——拦了登录接口自己 = 永远 401
 */
@WebFilter("/*")
public class LoginCheckFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String uri = request.getRequestURI();   // 如 /userapi/api/v1/users

        // ⓪ 预检请求（OPTIONS）：浏览器发 POST+JSON / DELETE 前先"问路"，这趟不带 Cookie
        //    ——必须放行，否则预检 401，真正的请求（新增/删除）根本发不出去（控制台报 CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);   // 交给 CorsFilter 回 200 + 跨域头
            return;
        }

        // ① 白名单：登录/退出接口放行（办证窗口不能也要证）
        boolean isLoginApi = uri.contains("/api/v1/auth/login");
        boolean isLogoutApi = uri.contains("/api/v1/auth/logout");
        if (isLoginApi || isLogoutApi) {
            chain.doFilter(request, response);
            return;
        }

        // ② 其余接口：查通行证
        HttpSession session = request.getSession(false);   // false：没有就 null，别新建
        Object user = (session == null) ? null : session.getAttribute("loginUser");

        if (user == null) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return;   // 拦下，不进 Servlet
        }

        // ③ 有通行证：放行
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
