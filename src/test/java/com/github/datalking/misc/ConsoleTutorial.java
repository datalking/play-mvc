package com.github.datalking.misc;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 获取控制台输出的示例
 */
public class ConsoleTutorial {

    public static void main(String[] args) {

        System.out.println("One");

        // 保存原来的out
        PrintStream newConsole = System.out;

        ByteArrayOutputStream consoleStorage = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consoleStorage));

        // 这里不打印，只存储到 consoleStorage
        System.out.println("two");

        // 这里打印
        newConsole.println(consoleStorage.toString());
        newConsole.println(consoleStorage.toString());

        // 还原out
        System.setOut(newConsole);

        System.out.println("three");
        System.out.println(consoleStorage.toString());
    }
}
