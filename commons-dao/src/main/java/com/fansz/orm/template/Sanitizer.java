package com.fansz.orm.template;

/**
 * 净化SQL
 */
public final class Sanitizer {
    private Sanitizer() {

    }

    /**
     * 危险字符
     */
    private static final String[] DANGER_SQL_KEYWORDS = { ";", "\"", "\'", "/*", "*/", "--", "exec", "select",
            "update", "delete", "insert", "alter", "drop", "create", "shutdown" };

    /**
     * 把SQL参数中的危险字符去除.
     *
     * @param sqlFraction 把SQL参数
     * @return 去除危险字符的SQL
     */
    public static String sanitizeSQL(String sqlFraction) {
        for (String keyword : DANGER_SQL_KEYWORDS) {
            sqlFraction = sqlFraction.replace(keyword, "");
        }
        return sqlFraction;
    }
}
