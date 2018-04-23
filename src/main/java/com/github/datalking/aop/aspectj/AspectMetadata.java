package com.github.datalking.aop.aspectj;

import com.github.datalking.aop.Pointcut;
import com.github.datalking.aop.support.ComposablePointcut;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.io.Serializable;

/**
 * aspect切面元数据类，持有pointcut
 *
 * @author yaoo on 4/19/18
 */
public class AspectMetadata implements Serializable {

    private final String aspectName;
    private final Class<?> aspectClass;
    private transient AjType<?> ajType;
    private Pointcut perClausePointcut;


    public AspectMetadata(Class<?> aspectClass, String aspectName) {
        this.aspectName = aspectName;

        /// 取到aspectClass的根父类
        Class<?> currClass = aspectClass;
        AjType<?> ajType = null;
        while (currClass != Object.class) {
            AjType<?> ajTypeToCheck = AjTypeSystem.getAjType(currClass);
            if (ajTypeToCheck.isAspect()) {
                ajType = ajTypeToCheck;
                break;
            }
            currClass = currClass.getSuperclass();
        }

        if (ajType == null) {
            throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
        }
        if (ajType.getDeclarePrecedence().length > 0) {
            throw new IllegalArgumentException("DeclarePrecendence not presently supported in current AOP");
        }
        this.aspectClass = ajType.getJavaClass();
        this.ajType = ajType;

        switch (this.ajType.getPerClause().getKind()) {
            case SINGLETON:
                this.perClausePointcut = Pointcut.TRUE;
                return;
            case PERTARGET:
            case PERTHIS:
                AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
                ajexp.setLocation(aspectClass.getName());
                ajexp.setExpression(findPerClause(aspectClass));
                ajexp.setPointcutDeclarationScope(aspectClass);
                this.perClausePointcut = ajexp;
                return;
            case PERTYPEWITHIN:
                this.perClausePointcut = new ComposablePointcut(new TypePatternClassFilter(findPerClause(aspectClass)));
                return;
            default:
                try {
                    throw new Exception("PerClause " + ajType.getPerClause().getKind() + " not supported by Spring AOP for " + aspectClass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private String findPerClause(Class<?> aspectClass) {
        String str = aspectClass.getAnnotation(Aspect.class).value();
        str = str.substring(str.indexOf("(") + 1);
        str = str.substring(0, str.length() - 1);
        return str;
    }


    public String getAspectName() {
        return aspectName;
    }

    public Class<?> getAspectClass() {
        return aspectClass;
    }

    public AjType<?> getAjType() {
        return ajType;
    }

    public Pointcut getPerClausePointcut() {
        return perClausePointcut;
    }

    public boolean isPerThisOrPerTarget() {
        PerClauseKind kind = getAjType().getPerClause().getKind();
        return (kind == PerClauseKind.PERTARGET || kind == PerClauseKind.PERTHIS);
    }

    public boolean isPerTypeWithin() {
        PerClauseKind kind = getAjType().getPerClause().getKind();
        return (kind == PerClauseKind.PERTYPEWITHIN);
    }


    public boolean isLazilyInstantiated() {
        return (isPerThisOrPerTarget() || isPerTypeWithin());
    }

}
