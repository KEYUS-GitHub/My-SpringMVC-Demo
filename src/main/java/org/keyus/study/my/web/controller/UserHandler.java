package org.keyus.study.my.web.controller;

import org.keyus.study.my.core.annotation.MyAutowired;
import org.keyus.study.my.core.annotation.MyController;
import org.keyus.study.my.core.annotation.MyRequestMapping;
import org.keyus.study.my.core.annotation.MyRequestParam;
import org.keyus.study.my.web.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@MyController
@MyRequestMapping("/user")
public class UserHandler {

    @MyAutowired("userService")
    private UserService userService;

    @MyRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                     @MyRequestParam("name") String name, @MyRequestParam("password")
                     String password) throws Exception {
        response.getWriter().write(userService.query(name, password));
    }
}
