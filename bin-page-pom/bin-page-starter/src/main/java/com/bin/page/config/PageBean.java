package com.bin.page.config;

import com.bin.mybatis.plugin.PagingPlugin;
import com.bin.page.aop.PageAop;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@Configurable
public class PageBean {
    /**
     * 装配Mybatis 分页插件
     * 默认启用
     */
    @Bean
    @ConditionalOnClass(SqlSessionFactory.class)
    @ConditionalOnProperty(prefix = "binPlugin.pagingPlugin", value = "enable", havingValue = "true", matchIfMissing = true)
    public PagingPlugin getPagePlugin() {
        return new PagingPlugin();
    }

    /**
     * AOP切面
     * 默认启用
     */
    @Bean
    @ConditionalOnBean(PagingPlugin.class)
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = "binPlugin.pagingPlugin", value = "enable", havingValue = "true", matchIfMissing = true)
    public PageAop getWebPageAop() {
        return new PageAop();
    }
}
