package org.example.aop.filter;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.TokenDTO;
import org.example.utils.JWTUtil;
import org.example.utils.StringTools;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 *
 */
@Component
@Slf4j
public class RequestFilter extends OncePerRequestFilter implements Filter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            //每个请求记录一个traceId,可以根据traceId搜索出本次请求的全部相关日志
            MDC.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 12));
            setToken(request);
            setProductId(request);
            //使request中的body可以重复读取 https://juejin.im/post/6858037733776949262#heading-4
            request = new ContentCachingRequestWrapper(request);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw e;
        } finally {
            //清理ThreadLocal
            MDC.clear();
        }
    }

    /**
     * 将url参数中的productId放入ThreadLocal
     */
    private void setProductId(HttpServletRequest request) {
        String productIdStr = request.getParameter("productId");
        if (!StringTools.isNullOrEmpty(productIdStr)) {
            log.debug("url中productId = {}", productIdStr);
            MDC.put("productId", productIdStr);
        }
    }

    private void setToken(HttpServletRequest request) {
        //通过token解析出username
        String token = request.getHeader("Authorization");
        if (StringTools.isNullOrEmpty(token)) {
            // 兼容部分客户端用自定义header传token
            token = request.getHeader("token");
        }
        if (StringTools.isNullOrEmpty(token)) {
            return;
        }
        // 兼容 Authorization: Bearer <token>
        if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length()).trim();
        }
        if (!StringTools.isNullOrEmpty(token)) {
            MDC.put("token", token);
        }
    }
}
