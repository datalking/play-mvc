package com1.github.datalking.controller;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.ResponseBody;
import com.github.datalking.web.mvc.Model;
import com1.github.datalking.bo.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomepageController {

    //    http://localhost:8999/
    @RequestMapping("/")
    public String welcome(Model model) {
//    public String welcome(Locale locale, Model model) {
//    public String welcome() {

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

    //    http://localhost:8999/user/sample1
    @ResponseBody
    @RequestMapping(value = "/user/sample1")
    public User getUser(Model model) {

        System.out.println("before getUser /user");
        User u1 = new User("userNameHere", 24);
        return u1;
    }

    @ResponseBody
    @RequestMapping(value = "/user/sample2")
    public Map<String, Object> getUser2(Model model) {

        System.out.println("before getUser /user2");
        Map<String, Object> map = new HashMap<>();
        User u1 = new User("userNameHere", 24);
        map.put("u1", u1);
        map.put("u2", "undefined");

        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/user/sample3", produces = "application/json")
    public List<User> getUser3(Model model) {

        System.out.println("before getUser /user3");
        return getUserList();
    }

    private List<User> getUserList() {
        List<User> lst = new ArrayList<>();
        User user1 = new User();
        user1.setName("aa");
        user1.setAge(10);
        lst.add(user1);
        User user2 = new User();
        user2.setName("bb");
        user2.setAge(33);
        lst.add(user2);
        return lst;
    }

}
