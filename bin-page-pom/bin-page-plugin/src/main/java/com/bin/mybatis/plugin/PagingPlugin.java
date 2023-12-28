package com.bin.mybatis.plugin;

import com.bin.mybatis.utils.SqlProcessingUtils;
import com.bin.page.entity.PagingInformation;
import com.bin.page.utils.PageThreadLocal;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Intercepts(
        @Signature(
                type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class, Integer.class}
        )
)
public class PagingPlugin implements Interceptor {

    private Logger log = LoggerFactory.getLogger(PagingPlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) SqlProcessingUtils.getStatementHandler(invocation.getTarget());
        MetaObject statementObject = SystemMetaObject.forObject(statementHandler);
        String sql = SqlProcessingUtils.getSql(statementObject);
        String mainSql = sql;
        if (!sql.startsWith("select")) {
            return invocation.proceed();
        }
        PagingInformation page = PageThreadLocal.getPagingInformation();
        if (page == null || !page.getEnable()) {
            return invocation.proceed();
        }
        Integer count = getCount(mainSql, sql, invocation, statementObject);
        page.setCount(count);

        if (page.getPageSize() == null || page.getPageSize() <= 0) {
            page.setPageSize(10);
        }

        int totalPage = page.getCount() % page.getPageSize() == 0 ?
                page.getCount() / page.getPageSize() :
                page.getCount() / page.getPageSize() + 1;
        page.setTotal(totalPage);

        if (page.getPageNum() <= 0) {
            page.setPageNum(1);
        } else if (page.getPageNum() > totalPage && totalPage > 0) {
            page.setPageNum(totalPage);
        }

        String limit = " limit " + ((page.getPageNum() - 1) * page.getPageSize()) + "," + page.getPageSize();

        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        if (sql.contains("limit")) {
            sql = sql.replaceAll("\\s*limit\\s+\\?", limit);
        } else {
            sql += limit;
        }

        statementObject.setValue("delegate.boundSql.sql", sql);
        log.debug("[PAGING SQL] exec sql - {}", sql);
        return invocation.proceed();
    }


    private Integer getCount(String mainSql, String sql, Invocation invocation, MetaObject statementObject) {
        int selectIndex;
        int limitIndex;
        int paramBegin = -1;
        int paramEnd = -1;
        boolean isSubQuery = false;
        if ((limitIndex = sql.indexOf("limit")) != -1) {
            isSubQuery = true;
            selectIndex = sql.lastIndexOf("select", limitIndex);
            mainSql = sql.substring(selectIndex, limitIndex);

            int beginIndex = 0;
            int parameterIndex;
            int parameterCount = 0;
            while ((parameterIndex = mainSql.indexOf("?", beginIndex)) != -1) {
                parameterCount++;
                beginIndex = parameterIndex + 1;
            }

            if (parameterCount > 0) {
                beginIndex = selectIndex;
                int bpCount = 0;
                while ((parameterIndex = sql.lastIndexOf("?", beginIndex)) != -1) {
                    bpCount++;
                    beginIndex = parameterIndex - 1;
                }

                paramBegin = bpCount;
                paramEnd = bpCount + parameterCount - 1;
            }
        }
        return getTotal(invocation, statementObject, mainSql, isSubQuery, paramBegin, paramEnd);
    }

    /**
     * 总数查询
     *
     * @return 符合查询条件的数量
     */
    private Integer getTotal(Invocation invocation, MetaObject statementObject, String sql, boolean isSubQuery, int beginParams, int endParams) {
        ParameterHandler ph = (ParameterHandler) statementObject.getValue("delegate.parameterHandler");
        String sqlPrefix = "select count(1) as total ";
        int formIndex = SqlProcessingUtils.getFromIndex(0, sql);
        String countSql = sqlPrefix + sql.substring(formIndex);
        int orderByIndex;

        if ((orderByIndex = countSql.indexOf("order by")) != -1) {
            countSql = countSql.substring(0, orderByIndex);
        }

        Connection conn = (Connection) invocation.getArgs()[0];
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(countSql);
            if (!isSubQuery) {
                ph.setParameters(ps);
            } else if (beginParams != -1 && endParams != -1) {
                setPreparedStatement(statementObject, beginParams, endParams, ps);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            log.error("[PagePlugin getTotal SQLException] 查询数量时SQL执行异常 --> " + e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.error("[PagePlugin getTotal SQLException] 查询数量时SQL执行finally异常 --> " + e);
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    log.error("[PagePlugin getTotal SQLException] 查询数量时SQL执行finally异常 --> " + e);
                }
            }
        }
        return 0;
    }


    /**
     * 根据传入的参数范围，从 SQL 的参数列表中获取参数，并将其设置到 PreparedStatement 中
     *
     * @param statementObject SQL 语句的对象。
     * @param beginParams     参数范围
     * @param endParams       参数范围
     * @param ps              预编译语句对象
     */
    private void setPreparedStatement(MetaObject statementObject, int beginParams, int endParams, PreparedStatement ps) {
        BoundSql boundSql = (BoundSql) statementObject.getValue("delegate.boundSql");
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        Object parameterObject = boundSql.getParameterObject();
        Configuration configuration = (Configuration) statementObject.getValue("delegate.configuration");
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        for (int i = beginParams; i <= endParams; i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            if (parameterMapping.getMode() != ParameterMode.OUT) {
                Object value;
                String propertyName = parameterMapping.getProperty();
                if (boundSql.hasAdditionalParameter(propertyName)) {
                    value = boundSql.getAdditionalParameter(propertyName);
                } else if (parameterObject == null) {
                    value = null;
                } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    value = parameterObject;
                } else {
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    value = metaObject.getValue(propertyName);
                }
                TypeHandler typeHandler = parameterMapping.getTypeHandler();
                JdbcType jdbcType = parameterMapping.getJdbcType();
                if (value == null && jdbcType == null) {
                    jdbcType = configuration.getJdbcTypeForNull();
                }
                try {
                    typeHandler.setParameter(ps, (i - beginParams) + 1, value, jdbcType);
                } catch (TypeException | SQLException e) {
                    throw new TypeException("Unmapped exception for mapping: " + parameterMapping + ". Cause: " + e, e);
                }
            }
        }
    }
}
