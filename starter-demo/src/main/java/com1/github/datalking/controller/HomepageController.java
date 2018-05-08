package com1.github.datalking.controller;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.ResponseBody;
import com.github.datalking.web.mvc.Model;
import com1.github.datalking.bo.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Home page controller. It has methods which are invoked by user
 * interactions on home page.
 */
@Controller
public class HomepageController {

    @RequestMapping("/")
    public String welcome(Locale locale, Model model) {

        System.out.println("before welcome /");

        return "home";
    }

    @RequestMapping("/test/ajax")
    public String testAjax() {

        System.out.println("before testAjax /test/ajax");

        return "ajax";
    }

    @ResponseBody
    @RequestMapping("/str")
    public String getSimpleStr() {

        System.out.println("before getSimpleStr /str");

        return "home";
    }

    @ResponseBody
    @RequestMapping(value = "/user/sample1", produces = "application/json")
    public User getUser(Model model) {
        System.out.println("before getUser /user");
        User u1 = new User("userNameHere", 24);

        return u1;
    }

    @ResponseBody
    @RequestMapping(value = "/user/sample2", produces = "application/json")
    public Map<String, Object> getUser2(Model model) {

        System.out.println("before getUser /user2");
        Map<String, Object> map = new HashMap<>();
        User u1 = new User("userNameHere", 24);
        map.put("u1", u1);
        map.put("u2", "undefined");

        return map;
    }


}
