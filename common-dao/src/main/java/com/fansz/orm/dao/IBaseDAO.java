package com.fansz.orm.dao;

import com.fansz.orm.dao.support.IQueryBuilder;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 基础DAO
 *
 * @param <T> 泛型
 */

/**
 * 基础DAO
 *
 * @param <T> 泛型
 */
public interface IBaseDAO<T> {
    /**
     * @param hibernateTemplate 实体管理器
     */
    void setHibernateTemplate(HibernateTemplate hibernateTemplate);

    /**
     * @return HibernateTemplate
     */
    HibernateTemplate getHibernateTemplate();

    /**
     * 获取查询构造器.
     *
     * @return IQueryBuilder
     */
    IQueryBuilder getQueryBuilder();

    /**
     * 设置查询构造器.
     *
     * @param queryBuilder 查询构造器
     */
    void setQueryBuilder(IQueryBuilder queryBuilder);

    /**
     * 获取记录总数.
     *
     * @return 记录总数
     */
    long getCount();

    /**
     * 清除一级缓存的数据.
     */
    void clear();

    /**
     * 主动提交事务.
     */
    void flush();

    /**
     * 保存实体.
     *
     * @param entity 实体
     */
    void save(T entity);

    /**
     * 更新实体.
     *
     * @param entity 实体
     */
    void update(T entity);

    /**
     * 删除实体.
     *
     * @param entityids 实体id数组
     */
    void delete(Serializable... entityids);

    /**
     * 获取实体.
     *
     * @param entityId 实体id
     * @return 实体对象
     */
    T load(Serializable entityId);

    List<T> findBy(Map<String, Object> parameters);

    /**
     * 一次查出所有记录.
     *
     * @return 实体对象集合
     */
    List<T> findAll();
}
