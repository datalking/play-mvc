package com.datable.excelbox.core;

import com.datable.excelbox.core.generator.DOMBasedExcelGenerator;
import com.datable.excelbox.core.parser.DOMBasedExcelParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * excel解析器与生成器的工厂类
 *
 * @author jinyaoo
 */
public class ExcelFactory {

    protected final static int DEFAULT_PARSER_FEATURE_FLAGS = ExcelParser.Feature.collectDefaults();
    protected final static int DEFAULT_GENERATOR_FEATURE_FLAGS = ExcelGenerator.Feature.collectDefaults();

    protected int parserFeatures = DEFAULT_PARSER_FEATURE_FLAGS;
    protected int generatorFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;

    public final ExcelFactory configure(ExcelParser.Feature f, boolean state) {
        return state ? enable(f) : disable(f);
    }

    public ExcelFactory enable(ExcelParser.Feature f) {
        parserFeatures |= f.getMask();
        return this;
    }

    public ExcelFactory disable(ExcelParser.Feature f) {
        parserFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(ExcelParser.Feature f) {
        return (parserFeatures & f.getMask()) != 0;
    }


    public ExcelParser createParser(String inputExcelPath) {

        FileInputStream in = null;

        try {
            in = new FileInputStream(new File(inputExcelPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new DOMBasedExcelParser(in, parserFeatures);
    }


    public ExcelParser createParser(File file) {

        return null;
    }

    public ExcelParser createParser(InputStream in) {
        return new DOMBasedExcelParser(in, parserFeatures);
    }


    public ExcelGenerator createGenerator(String outputExcelPath) {
        OutputStream out  = null;
        try {
            out = new FileOutputStream(outputExcelPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return createGenerator(out);
    }

    public ExcelGenerator createGenerator(File file) {

        return null;
    }

    public ExcelGenerator createGenerator(OutputStream out) {

        return new DOMBasedExcelGenerator(out, generatorFeatures);
    }

}
