package com.bin.mybatis.utils;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

public class SqlProcessingUtils {
    /**
     * 获取StatementHandler处理对象
     */
    public static Object getStatementHandler(Object target) {
        MetaObject metaObject = SystemMetaObject.forObject(target);
        while (metaObject.hasGetter("h")) {
            target = metaObject.getValue("h.target");
            metaObject = SystemMetaObject.forObject(target);
        }
        return target;
    }

    /**
     * 获取并修改sql格式
     */
    public static String getSql(MetaObject metaObject) {
        BoundSql boundSql = (BoundSql) metaObject
                .getValue("delegate.boundSql");
        return boundSql.getSql()
                .toLowerCase()
                .trim()
                .replace("\n", "")
                .replaceAll("\\s+", " ");
    }


    /**
     * @param beginIndex 起始位置
     * @param sql        要查询的sql
     * @return from下标
     */
    public static int getFromIndex(int beginIndex, String sql) {
        int fromIndex = sql.indexOf("from", beginIndex);
        int leftCount = 0;
        int rightCount = 0;
        int index = 0;
        while (index < fromIndex) {
            if (sql.charAt(index) == '(') {
                leftCount++;
            } else if (sql.charAt(index) == ')') {
                rightCount++;
            }
            index++;
        }
        return leftCount == rightCount ? fromIndex : getFromIndex(fromIndex + 4, sql);
    }
}
