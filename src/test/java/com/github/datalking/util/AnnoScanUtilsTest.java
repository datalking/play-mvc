package com.github.datalking.util;

import com.github.datalking.bean1.anno.WebMvcConfig;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * AnnoScanUtils Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>May 6, 2018</pre>
 */
public class AnnoScanUtilsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testIsCandidateComponent() throws Exception {
    }

    @Test
    public void testGetAnnoClassIncludingSuper() throws Exception {
        Class clazz = WebMvcConfig.class;
        Set<Class> annoAll = AnnoScanUtils.getAnnoClassIncludingSuper(clazz);
        assertEquals(9, annoAll.size());
    }

}
