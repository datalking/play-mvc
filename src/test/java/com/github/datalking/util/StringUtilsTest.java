package com.github.datalking.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.junit.Assert.assertEquals;

/**
 * StringUtils Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Apr 23, 2018</pre>
 */
public class StringUtilsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: isEmpty(String str)
     */
    @Test
    public void testIsEmpty() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: isNotEmpty(String str)
     */
    @Test
    public void testIsNotEmpty() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: firstLetterUpperCase(String original)
     */
    @Test
    public void testFirstLetterUpperCase() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: replace(String inString, String oldPattern, String newPattern)
     */
    @Test
    public void testReplace() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: hasLength(String str)
     */
    @Test
    public void testHasLengthStr() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: hasText(String input)
     */
    @Test
    public void testHasText() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens)
     */
    @Test
    public void testTokenizeToStringArrayForStrDelimitersTrimTokensIgnoreEmptyTokens() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: tokenizeToStringArray(String str, String delimiters)
     */
    @Test
    public void testTokenizeToStringArrayForStrDelimiters() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: trimWhitespace(String str)
     */
    @Test
    public void testTrimWhitespace() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: commaDelimitedListToStringArray(String str)
     */
    @Test
    public void testCommaDelimitedListToStringArray() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: delimitedListToStringArray(String str, String delimiter)
     */
    @Test
    public void testDelimitedListToStringArrayForStrDelimiter() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: delimitedListToStringArray(String str, String delimiter, String charsToDelete)
     */
    @Test
    public void testDelimitedListToStringArrayForStrDelimiterCharsToDelete() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: deleteAny(String inString, String charsToDelete)
     */
    @Test
    public void testDeleteAny() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: countWordFromStr1(String srcText, String keyword)
     */
    @Test
    public void testCountWordFromStr1() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: countWordFromStr(String srcText, String keyword)
     */
    @Test
    public void testCountWordFromStr() throws Exception {
        String src = "====print before1";
        String key = "print";
        int c = StringUtils.countWordFromStr(src, key);
        assertEquals(1, c);
    }


}
