package com.github.datalking.bean;

/**
 * @author yaoo on 4/10/18
 */
public class FooService implements FooInterface {

    private String text = "aabbcc";

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String printInnerText() {
        //System.out.println(text);
        return text;
    }

}
