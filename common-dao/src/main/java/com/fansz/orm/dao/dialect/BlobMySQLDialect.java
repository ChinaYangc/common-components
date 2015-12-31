package com.fansz.orm.dao.dialect;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.type.TextType;

/**
 * 扩展Mysql Dialect,支持新的类型
 * 
 * @author yanyanming
 */
public class BlobMySQLDialect extends MySQLDialect {
    public BlobMySQLDialect() {
        super();
        registerHibernateType(Types.LONGVARCHAR, Hibernate.TEXT.getName());
    }
}
