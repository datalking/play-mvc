package com.github.datalking.web.bind;

import com.github.datalking.common.MessageCodesResolver;
import com.github.datalking.common.convert.ConversionService;
import com.github.datalking.web.context.request.WebRequest;

/**
 * @author yaoo on 5/4/18
 */
public class ConfigurableWebBindingInitializer implements WebBindingInitializer {

    private boolean autoGrowNestedPaths = true;

    private boolean directFieldAccess = false;

    private MessageCodesResolver messageCodesResolver;

//    private BindingErrorProcessor bindingErrorProcessor;

//    private Validator validator;

    private ConversionService conversionService;

//    private PropertyEditorRegistrar[] propertyEditorRegistrars;

    public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
        this.autoGrowNestedPaths = autoGrowNestedPaths;
    }

    public boolean isAutoGrowNestedPaths() {
        return this.autoGrowNestedPaths;
    }

    public final void setDirectFieldAccess(boolean directFieldAccess) {
        this.directFieldAccess = directFieldAccess;
    }

    public boolean isDirectFieldAccess() {
        return directFieldAccess;
    }

    public final void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
        this.messageCodesResolver = messageCodesResolver;
    }

    public final MessageCodesResolver getMessageCodesResolver() {
        return this.messageCodesResolver;
    }
//
//    public final void setBindingErrorProcessor(BindingErrorProcessor bindingErrorProcessor) {
//        this.bindingErrorProcessor = bindingErrorProcessor;
//    }
//
//    public final BindingErrorProcessor getBindingErrorProcessor() {
//        return this.bindingErrorProcessor;
//    }
//
//    public final void setValidator(Validator validator) {
//        this.validator = validator;
//    }
//
//    public final Validator getValidator() {
//        return this.validator;
//    }

    public final void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public final ConversionService getConversionService() {
        return this.conversionService;
    }

//    public final void setPropertyEditorRegistrar(PropertyEditorRegistrar propertyEditorRegistrar) {
//        this.propertyEditorRegistrars = new PropertyEditorRegistrar[] {propertyEditorRegistrar};
//    }
//
//    public final void setPropertyEditorRegistrars(PropertyEditorRegistrar[] propertyEditorRegistrars) {
//        this.propertyEditorRegistrars = propertyEditorRegistrars;
//    }
//
//    public final PropertyEditorRegistrar[] getPropertyEditorRegistrars() {
//        return this.propertyEditorRegistrars;
//    }

    public void initBinder(WebDataBinder binder, WebRequest request) {
        binder.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
        if (this.directFieldAccess) {
            binder.initDirectFieldAccess();
        }
        if (this.messageCodesResolver != null) {
            binder.setMessageCodesResolver(this.messageCodesResolver);
        }
//        if (this.bindingErrorProcessor != null) {
//            binder.setBindingErrorProcessor(this.bindingErrorProcessor);
//        }
//        if (this.validator != null && binder.getTarget() != null &&
//                this.validator.supports(binder.getTarget().getClass())) {
//            binder.setValidator(this.validator);
//        }
        if (this.conversionService != null) {
            binder.setConversionService(this.conversionService);
        }
//        if (this.propertyEditorRegistrars != null) {
//            for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
//                propertyEditorRegistrar.registerCustomEditors(binder);
//            }
//        }
    }

}
