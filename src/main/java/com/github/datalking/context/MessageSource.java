package com.github.datalking.context;

import java.util.Locale;

/**
 * message解析接口
 * 支持语言国际化
 *
 * @author yaoo on 5/3/18
 */
public interface MessageSource {

    String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

    String getMessage(String code, Object[] args, Locale locale);

    String getMessage(MessageSourceResolvable resolvable, Locale locale);

}
