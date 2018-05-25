package com.github.datalking.web.bind;

import com.github.datalking.annotation.InitBinder;
import com.github.datalking.web.context.request.WebRequest;
import com.github.datalking.web.mvc.method.HandlerMethod;
import com.github.datalking.web.servlet.InvocableHandlerMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author yaoo on 5/2/18
 */
public class InitBinderDataBinderFactory extends DefaultDataBinderFactory {

    private final List<InvocableHandlerMethod> binderMethods;

    public InitBinderDataBinderFactory(List<InvocableHandlerMethod> binderMethods, WebBindingInitializer initializer) {
        super(initializer);
        this.binderMethods = (binderMethods != null) ? binderMethods : new ArrayList<>();
    }

    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) throws Exception {

        for (InvocableHandlerMethod binderMethod : this.binderMethods) {

            if (isBinderMethodApplicable(binderMethod, binder)) {

                Object returnValue = binderMethod.invokeForRequest(request, null, binder);

                if (returnValue != null) {
                    throw new IllegalStateException("@InitBinder methods should return void: " + binderMethod);
                }
            }
        }
    }

    protected boolean isBinderMethodApplicable(HandlerMethod initBinderMethod, WebDataBinder binder) {
        InitBinder annot = initBinderMethod.getMethodAnnotation(InitBinder.class);
        Collection<String> names = Arrays.asList(annot.value());

        return (names.size() == 0 || names.contains(binder.getObjectName()));
    }

}
