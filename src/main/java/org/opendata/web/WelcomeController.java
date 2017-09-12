package org.opendata.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class WelcomeController {

    @RequestMapping("/welcome")
    public ModelAndView test() {
        return new ModelAndView("index");
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showMenu(Model model) {
        model.addAttribute("today", new Date());
        return "index";
    }

}