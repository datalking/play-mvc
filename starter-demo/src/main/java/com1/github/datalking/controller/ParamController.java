package com1.github.datalking.controller;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.PathVariable;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.annotation.web.RequestParam;
import com.github.datalking.web.mvc.Model;
import com1.github.datalking.bo.User;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ParamController {

//    http://localhost:8999/param/model
    @RequestMapping("/param/model")
    public String testModel(Model model) {
        model.addAttribute("userList", getUserList());
        return "user-info";
    }

//    http://localhost:8999/param/path/var/12
    @RequestMapping("/param/path/var/{v}")
    public String testPathVariable(@PathVariable(value = "v") Integer param1, Model model) {
        System.out.println("/param/path/var/{v}:" + param1);
        model.addAttribute("v", param1);
        model.addAttribute("userList", getUserList());

        return "param-simple";
    }

//    http://localhost:8999/param/req?v1=str&v3=33
    @RequestMapping("/param/req")
    public String testRequestParam(@RequestParam(value = "v1") String v1, @RequestParam(value = "v3") Integer v2, Model model) {
        System.out.println("RequestParam:" + v1 + ", " + v2);
        model.addAttribute("v1", v1);
        model.addAttribute("v2", v2);

        return "param-simple";
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

