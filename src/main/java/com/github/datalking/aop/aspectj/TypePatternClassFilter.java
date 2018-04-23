package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.ClassFilter;
import com.github.datalking.util.Assert;
import com.github.datalking.util.StringUtils;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;

/**
 * aspectj切点表达式匹配执行类
 *
 * @author yaoo on 4/19/18
 */
public class TypePatternClassFilter implements ClassFilter {

    private String typePattern;

    private TypePatternMatcher aspectJTypePatternMatcher;

    public TypePatternClassFilter() {
    }

    public TypePatternClassFilter(String typePattern) {
        setTypePattern(typePattern);
    }

    public void setTypePattern(String typePattern) {
        Assert.notNull(typePattern, "Type pattern must not be null");
        this.typePattern = typePattern;
        String pattern = replaceBooleanOperators(typePattern);
        this.aspectJTypePatternMatcher = PointcutParser
                .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution()
                .parseTypePattern(pattern);
    }

    public String getTypePattern() {
        return this.typePattern;
    }


    /**
     * 执行匹配
     */
    @Override
    public boolean matches(Class<?> clazz) {
        return this.aspectJTypePatternMatcher.matches(clazz);
    }

    private String replaceBooleanOperators(String pcExpr) {
        String result = StringUtils.replace(pcExpr, " and ", " && ");
        result = StringUtils.replace(result, " or ", " || ");
        return StringUtils.replace(result, " not ", " ! ");
    }


}
