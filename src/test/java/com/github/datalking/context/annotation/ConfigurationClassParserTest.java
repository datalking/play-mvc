package com.github.datalking.context.annotation;

import com.github.datalking.annotation.Component;
import com.github.datalking.annotation.EnableAspectJAutoProxy;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;


/**
 * ConfigurationClassParser Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Apr 17, 2018</pre>
 */
@Component
@EnableAspectJAutoProxy
public class ConfigurationClassParserTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testParseEnableXxxAnno() throws Exception {

        Annotation[] annos = ConfigurationClassParserTest.class.getAnnotations();

//        for (Annotation anno : annos) {
//
//            System.out.println(anno.annotationType().getName());
//
//            for (Annotation a2 : anno.annotationType().getAnnotations()) {
//                System.out.println("==== " + a2.annotationType().getName());
//
//            }
//            System.out.println();
//        }

        assertEquals("2", annos.length + "");

    }


}
