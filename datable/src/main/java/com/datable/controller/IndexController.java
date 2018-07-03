package com.datable.controller;

import com.github.datalking.annotation.Controller;
import com.github.datalking.annotation.web.RequestMapping;
import com.github.datalking.web.http.RequestMethod;
import com.github.datalking.web.mvc.Model;

/**
 * @author yaoo on 6/30/18
 */
@Controller
@RequestMapping("/")
public class IndexController {

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String homeIndexPage(Model model) {

        return "index";
    }

}
