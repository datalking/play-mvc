package com.github.datalking.web.mvc.condition;

import javax.servlet.http.HttpServletRequest;

/**
 * 保存从request提取出的用于匹配handler的条件
 *
 * @author yaoo on 4/28/18
 */
public interface RequestCondition<T> {

    T combine(T other);

    T getMatchingCondition(HttpServletRequest request);

    int compareTo(T other, HttpServletRequest request);

}
