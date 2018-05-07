package com1.github.datalking.controller;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.ResponseBody;

@Controller
public class HomepageController {

    @RequestMapping("/")
    public String welcome() {

        System.out.println("before response /");

        return "home";
    }

    @ResponseBody
    @RequestMapping("/str")
    public String getSimpleStr() {

        System.out.println("before response /str");

        return "home";
    }

}
