package com.bin.page.aop;

import com.bin.page.entity.PagingInformation;
import com.bin.page.result.PageResult;
import com.bin.page.utils.PageThreadLocal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取请求的分页信息
 */
@Aspect
public class PageAop {
    @Value("${binPlugin.pagingPlugin.name.pageNum:pageNum}")
    private String pageNum;

    @Value("${binPlugin.pagingPlugin.name.pageSize:pageSize}")
    private String pageSize;

    @Value("${binPlugin.pagingPlugin.name.total:total}")
    private String pTotal;

    @Value("${binPlugin.pagingPlugin.name.count:count}")
    private String pCount;

    @Around("@within(org.springframework.web.bind.annotation.RestController) || @within(org.springframework.stereotype.Controller)")
    public Object pageHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String requestPageNum = request.getParameter(pageNum);
        String requestPageSize = request.getParameter(pageSize);
        if (requestPageNum != null && requestPageSize != null) {
            PageThreadLocal.setPagingInformation(Integer.valueOf(requestPageNum), Integer.valueOf(requestPageSize));
        }
        Object result = joinPoint.proceed();
        PagingInformation page = PageThreadLocal.getPagingInformation();
        if (page != null) {
            if (result instanceof PageResult) {
                Map<String, Integer> pageMap = new HashMap<>();
                pageMap.put(pageNum, page.getPageNum());
                pageMap.put(pageSize, page.getPageSize());
                pageMap.put(pTotal, page.getTotal());
                pageMap.put(pCount, page.getCount());
                ((PageResult) result).setPage(pageMap);
            }
            PageThreadLocal.clearPage();
        }
        return result;
    }
}
