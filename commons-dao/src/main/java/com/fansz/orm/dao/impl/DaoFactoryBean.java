package com.fansz.orm.dao.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.Resource;

import com.fansz.orm.dao.IBaseDAO;
import com.fansz.pub.utils.GenericTools;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fansz.pub.utils.BeanTools;

/**
 * 动态生成DAO实现的工厂类
 */
@SuppressWarnings("rawtypes")
public class DaoFactoryBean implements FactoryBean, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(DaoFactoryBean.class);

    // Spring ApplicationContext
    private ApplicationContext applicationContext;

    // 动态生成DAO的方法实现
    @Resource(name = "daoMethodInterceptor")
    private MethodInterceptor methodInterceptor;

    // 可以是接口或抽象类
    private Class<?> daoClass;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws IllegalAccessException, InstantiationException {
        Enhancer e = new Enhancer();
        Object dao;
        // 如果是接口
        if (daoClass.isInterface()) {
            e.setInterfaces(new Class[] { daoClass });
            // 如果扩展了IBaseDAO, 则要继承BaseDAOImpl
            if (IBaseDAO.class.isAssignableFrom(daoClass)) {
                e.setSuperclass(BaseDAOImpl.class);
            }
            e.setCallbacks(new Callback[] { NoOp.INSTANCE, methodInterceptor });
            e.setCallbackFilter(new DaoCallbackFilter());
            dao = e.create();
            if (IBaseDAO.class.isAssignableFrom(daoClass)) {
                changeEntityClass(dao, daoClass);
            }
        }
        else if (Modifier.isAbstract(daoClass.getModifiers())) {// 如果是抽象类
            e.setSuperclass(daoClass);
            e.setCallbacks(new Callback[] { NoOp.INSTANCE, methodInterceptor });
            e.setCallbackFilter(new DaoCallbackFilter());
            dao = e.create();
            if (IBaseDAO.class.isAssignableFrom(daoClass)) {
                changeEntityClass(dao, daoClass);
            }
        }
        else {// 实现类
            dao = daoClass.newInstance();
        }
        applicationContext.getAutowireCapableBeanFactory().autowireBean(dao);
        return dao;
    }

    /**
     * 设置DAO实例的entityClass属性
     *
     * @param dao DAO实例
     * @param daoIfaceClass 接口
     */
    private static void changeEntityClass(Object dao, Class<?> daoIfaceClass) {
        try {
            Class<?> typeClass = GenericTools.getSuperClassGenericType(daoIfaceClass);
            BeanTools.setFieldValue(dao, "entityClass", typeClass);
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return daoClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Class<?> getDaoClass() {
        return daoClass;
    }

    public void setDaoClass(Class<?> daoClass) {
        this.daoClass = daoClass;
    }

    public MethodInterceptor getMethodInterceptor() {
        return methodInterceptor;
    }

    public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
        this.methodInterceptor = methodInterceptor;
    }

    /**
     * 静态内部类，用于过滤回调方法
     */
    static class DaoCallbackFilter implements CallbackFilter {
        @Override
        public int accept(Method method) {
            return Modifier.isAbstract(method.getModifiers()) ? 1 : 0;
        }
    }
}
