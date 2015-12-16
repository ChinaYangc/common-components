package com.fansz.orm.dao.impl;

import com.fansz.orm.dao.DaoScanner;
import com.fansz.orm.dao.annotation.DAO;
import com.fansz.pub.exception.FrameworkException;
import com.fansz.pub.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import javax.annotation.Resource;
import java.util.List;

/**
 * 注册DAO的Bean到Spring的环境里
 */
public class DaoBeanRegisterer implements BeanFactoryPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DaoBeanRegisterer.class);

    @Resource(name = "daoScanner")
    private DaoScanner daoScanner;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) {
        List<Class<?>> daoClassList = daoScanner.findDaoClasses();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry)factory;

        for (Class<?> daoClass : daoClassList) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(DaoFactoryBean.class);
            beanDefinition.setLazyInit(false);
            beanDefinition.setAbstract(false);
            beanDefinition.setAutowireCandidate(true);
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            MutablePropertyValues propertyValues = new MutablePropertyValues();
            propertyValues.add("daoClass", daoClass);
            beanDefinition.setPropertyValues(propertyValues);

            // 检查标注是否合法
            DAO daoAnn = (DAO)daoClass.getAnnotation(DAO.class);
            if (daoAnn == null || StringTools.isBlank(daoAnn.value())) {
                throw new FrameworkException("can not find dao name for class : " + daoClass);
            }
            registry.registerBeanDefinition(daoAnn.value(), beanDefinition);

            LOG.info("registering dao bean {}:{} to spring application context", daoAnn.value(), daoClass);
        }

    }

    public void setDaoScanner(DaoScanner daoScanner) {
        this.daoScanner = daoScanner;
    }
}
