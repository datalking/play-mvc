package com.github.datalking.util;

import com.github.datalking.util.ResourceUtils;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ResourceUtils Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Apr 9, 2018</pre>
 */
public class ResourceUtilsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getAllClassFromPackage(String fullyQualifiedPack, boolean recursive)
     */
    @Test
    public void testGetAllClassFromPackage() throws Exception {

        Set<Class> classSet = ResourceUtils.getAllClassFromPackage("com.github.datalking.context", true);

//        for (Class c : classSet) {
//            System.out.println(c.getName());
//        }

        assertNotNull(classSet);

    }


}
