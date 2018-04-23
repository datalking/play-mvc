package com.github.datalking.aop.support;

import com.github.datalking.aop.ClassFilter;
import com.github.datalking.util.Assert;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author yaoo on 4/19/18
 */
public abstract class ClassFilters {


    public static ClassFilter union(ClassFilter cf1, ClassFilter cf2) {
        Assert.notNull(cf1, "First ClassFilter must not be null");
        Assert.notNull(cf2, "Second ClassFilter must not be null");
        return new UnionClassFilter(new ClassFilter[]{cf1, cf2});
    }

    public static ClassFilter intersection(ClassFilter cf1, ClassFilter cf2) {
        Assert.notNull(cf1, "First ClassFilter must not be null");
        Assert.notNull(cf2, "Second ClassFilter must not be null");
        return new IntersectionClassFilter(new ClassFilter[]{cf1, cf2});
    }


    private static class UnionClassFilter implements ClassFilter, Serializable {

        public ClassFilter[] filters;

        public UnionClassFilter(ClassFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            for (ClassFilter filter : this.filters) {
                if (filter.matches(clazz)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object other) {
            return (this == other ||
                    (other instanceof UnionClassFilter && Arrays.equals(this.filters, ((UnionClassFilter) other).filters)));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.filters);
        }
    }

    private static class IntersectionClassFilter implements ClassFilter, Serializable {

        private ClassFilter[] filters;

        public IntersectionClassFilter(ClassFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean matches(Class<?> clazz) {
            for (ClassFilter filter : this.filters) {
                if (!filter.matches(clazz)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object other) {
            return (this == other || (other instanceof IntersectionClassFilter &&
                    Arrays.equals(this.filters, ((IntersectionClassFilter) other).filters)));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.filters);
        }
    }


}
