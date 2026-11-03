package com.example;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域放行（第 9 章 CorsFilter 的升级版）：
 * 前端 5173 调后端 8080 是跨源；本章登录要靠 JSESSIONID Cookie 传通行证，
 * 带 Cookie 时 Origin 不能是 *，必须写明确的前端源 + Allow-Credentials: true
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // 1. 允许的前端源（带 Cookie 时不能写 *）
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        response.setHeader("Access-Control-Allow-Credentials", "true");   // 允许前端带 Cookie
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        // 2. 预检请求（OPTIONS）：直接放行，别进 Servlet
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(200);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
