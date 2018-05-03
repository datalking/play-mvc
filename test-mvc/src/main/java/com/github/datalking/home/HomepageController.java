package com.github.datalking.home;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;

@Controller
public class HomepageController {

    @RequestMapping("/")
    public String welcome() {

        System.out.println("before response /");

        return "home";
    }

}
