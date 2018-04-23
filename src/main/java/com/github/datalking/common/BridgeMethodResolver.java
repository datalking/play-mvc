package com.github.datalking.common;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yaoo on 4/19/18
 */
public abstract class BridgeMethodResolver {

//    public static Method findBridgedMethod(Method bridgeMethod) {
//        if (bridgeMethod == null || !bridgeMethod.isBridge()) {
//            return bridgeMethod;
//        }
//        // Gather all methods with matching name and parameter size.
//        List<Method> candidateMethods = new ArrayList<Method>();
//        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bridgeMethod.getDeclaringClass());
//        for (Method candidateMethod : methods) {
//            if (isBridgedCandidateFor(candidateMethod, bridgeMethod)) {
//                candidateMethods.add(candidateMethod);
//            }
//        }
//        // Now perform simple quick check.
//        if (candidateMethods.size() == 1) {
//            return candidateMethods.get(0);
//        }
//        // Search for candidate match.
//        Method bridgedMethod = searchCandidates(candidateMethods, bridgeMethod);
//        if (bridgedMethod != null) {
//            // Bridged method found...
//            return bridgedMethod;
//        } else {
//            // A bridge method was passed in but we couldn't find the bridged method.
//            // Let's proceed with the passed-in method and hope for the best...
//            return bridgeMethod;
//        }
//    }
//
//
//    private static boolean isBridgedCandidateFor(Method candidateMethod, Method bridgeMethod) {
//        return (!candidateMethod.isBridge() && !candidateMethod.equals(bridgeMethod) &&
//                candidateMethod.getName().equals(bridgeMethod.getName()) &&
//                candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length);
//    }
//
//    private static Method searchCandidates(List<Method> candidateMethods, Method bridgeMethod) {
//        if (candidateMethods.isEmpty()) {
//            return null;
//        }
//        Method previousMethod = null;
//        boolean sameSig = true;
//        for (Method candidateMethod : candidateMethods) {
//            if (isBridgeMethodFor(bridgeMethod, candidateMethod, bridgeMethod.getDeclaringClass())) {
//                return candidateMethod;
//            } else if (previousMethod != null) {
//                sameSig = sameSig &&
//                        Arrays.equals(candidateMethod.getGenericParameterTypes(), previousMethod.getGenericParameterTypes());
//            }
//            previousMethod = candidateMethod;
//        }
//        return (sameSig ? candidateMethods.get(0) : null);
//    }
//
//    static boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Class<?> declaringClass) {
//        if (isResolvedTypeMatch(candidateMethod, bridgeMethod, declaringClass)) {
//            return true;
//        }
////        Method method = findGenericDeclaration(bridgeMethod);
////        return (method != null && isResolvedTypeMatch(method, candidateMethod, declaringClass));
//        return false;
//    }
//
//    private static boolean isResolvedTypeMatch(Method genericMethod, Method candidateMethod, Class<?> declaringClass) {
//        Type[] genericParameters = genericMethod.getGenericParameterTypes();
//        Class<?>[] candidateParameters = candidateMethod.getParameterTypes();
//        if (genericParameters.length != candidateParameters.length) {
//            return false;
//        }
//        for (int i = 0; i < candidateParameters.length; i++) {
//
//            ResolvableType genericParameter = ResolvableType.forMethodParameter(genericMethod, i, declaringClass);
//            Class<?> candidateParameter = candidateParameters[i];
//             // An array type: compare the component type.
////            if (candidateParameter.isArray()) {
////                if (!candidateParameter.getComponentType().equals(genericParameter.getComponentType().resolve(Object.class))) {
////                    return false;
////                }
////            }
//            // A non-array type: compare the type itself.
//            if (!candidateParameter.equals(genericParameter.resolve(Object.class))) {
//                return false;
//            }
//        }
//        return true;
//    }

}
