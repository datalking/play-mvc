package com.github.datalking.exception;

import com.github.datalking.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author yaoo on 5/29/18
 */
public class NoUniqueBeanDefinitionException extends NoSuchBeanDefinitionException {

    private int numberOfBeansFound;

    public NoUniqueBeanDefinitionException(Class<?> type, int numberOfBeansFound, String message) {
        super(type, message);
        this.numberOfBeansFound = numberOfBeansFound;
    }

    public NoUniqueBeanDefinitionException(Class<?> type, Collection<String> beanNamesFound) {
        this(type, beanNamesFound.size(), "expected single matching bean but found " + beanNamesFound.size() + ": " +
                StringUtils.collectionToCommaDelimitedString(beanNamesFound));
    }

    public NoUniqueBeanDefinitionException(Class<?> type, String... beanNamesFound) {
        this(type, Arrays.asList(beanNamesFound));
    }

    @Override
    public int getNumberOfBeansFound() {
        return this.numberOfBeansFound;
    }

}
