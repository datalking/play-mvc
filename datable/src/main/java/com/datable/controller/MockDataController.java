package com.datable.controller;

import com.datable.model.UserExcelInfo;
import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yaoo on 7/7/18
 */
@Controller
@RequestMapping("/v0/")
public class MockDataController {

    @ResponseBody
    @RequestMapping("/user/excel/info")
    public List<UserExcelInfo> listUserExcelInfo() {

        List<UserExcelInfo> list = new ArrayList<>();

        UserExcelInfo u1 = new UserExcelInfo("u1", 11);
        UserExcelInfo u2 = new UserExcelInfo("u2", 22);
        list.add(u1);
        list.add(u2);

        return list;
    }

}
