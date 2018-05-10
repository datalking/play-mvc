package com.github.datalking.common.convert;

import com.github.datalking.common.convert.converter.ConverterRegistry;
import com.github.datalking.common.convert.support.ArrayToCollectionConverter;
import com.github.datalking.common.convert.support.CollectionToArrayConverter;
import com.github.datalking.common.convert.support.CollectionToCollectionConverter;
import com.github.datalking.common.convert.support.CollectionToObjectConverter;
import com.github.datalking.common.convert.support.NumberToNumberConverterFactory;
import com.github.datalking.common.convert.support.ObjectToCollectionConverter;
import com.github.datalking.common.convert.support.ObjectToObjectConverter;
import com.github.datalking.common.convert.support.ObjectToStringConverter;
import com.github.datalking.common.convert.support.StringToArrayConverter;
import com.github.datalking.common.convert.support.StringToBooleanConverter;
import com.github.datalking.common.convert.support.StringToNumberConverterFactory;

import java.util.Locale;
import java.util.UUID;

/**
 * 转换器的默认实现
 *
 * @author yaoo on 5/10/18
 */
public class DefaultConversionService extends GenericConversionService {

    public DefaultConversionService() {
        addDefaultConverters(this);
    }

    // static utility methods

    public static void addDefaultConverters(ConverterRegistry converterRegistry) {
        addScalarConverters(converterRegistry);
        addCollectionConverters(converterRegistry);
        addFallbackConverters(converterRegistry);
    }

    // internal helpers

    private static void addScalarConverters(ConverterRegistry converterRegistry) {
        ConversionService conversionService = (ConversionService) converterRegistry;
        converterRegistry.addConverter(new StringToBooleanConverter());
        converterRegistry.addConverter(Boolean.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverterFactory(new StringToNumberConverterFactory());
        converterRegistry.addConverter(Number.class, String.class, new ObjectToStringConverter());

        converterRegistry.addConverterFactory(new NumberToNumberConverterFactory());

//        converterRegistry.addConverter(new StringToCharacterConverter());
        converterRegistry.addConverter(Character.class, String.class, new ObjectToStringConverter());

//        converterRegistry.addConverter(new NumberToCharacterConverter());
//        converterRegistry.addConverterFactory(new CharacterToNumberFactory());

//        converterRegistry.addConverterFactory(new StringToEnumConverterFactory());
//        converterRegistry.addConverter(Enum.class, String.class, new EnumToStringConverter(conversionService));

//        converterRegistry.addConverter(new StringToLocaleConverter());
        converterRegistry.addConverter(Locale.class, String.class, new ObjectToStringConverter());

//        converterRegistry.addConverter(new PropertiesToStringConverter());
//        converterRegistry.addConverter(new StringToPropertiesConverter());

//        converterRegistry.addConverter(new StringToUUIDConverter());
        converterRegistry.addConverter(UUID.class, String.class, new ObjectToStringConverter());
    }

    private static void addCollectionConverters(ConverterRegistry converterRegistry) {
        ConversionService conversionService = (ConversionService) converterRegistry;
        converterRegistry.addConverter(new ArrayToCollectionConverter(conversionService));
        converterRegistry.addConverter(new CollectionToArrayConverter(conversionService));

//        converterRegistry.addConverter(new ArrayToArrayConverter(conversionService));
        converterRegistry.addConverter(new CollectionToCollectionConverter(conversionService));
//        converterRegistry.addConverter(new MapToMapConverter(conversionService));

//        converterRegistry.addConverter(new ArrayToStringConverter(conversionService));
        converterRegistry.addConverter(new StringToArrayConverter(conversionService));

//        converterRegistry.addConverter(new ArrayToObjectConverter(conversionService));
//        converterRegistry.addConverter(new ObjectToArrayConverter(conversionService));

//        converterRegistry.addConverter(new CollectionToStringConverter(conversionService));
//        converterRegistry.addConverter(new StringToCollectionConverter(conversionService));

        converterRegistry.addConverter(new CollectionToObjectConverter(conversionService));
        converterRegistry.addConverter(new ObjectToCollectionConverter(conversionService));
    }

    private static void addFallbackConverters(ConverterRegistry converterRegistry) {
        ConversionService conversionService = (ConversionService) converterRegistry;
        converterRegistry.addConverter(new ObjectToObjectConverter());
//        converterRegistry.addConverter(new IdToEntityConverter(conversionService));
//        converterRegistry.addConverter(new FallbackObjectToStringConverter());
    }

}
