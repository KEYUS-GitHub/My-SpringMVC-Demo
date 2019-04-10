package org.keyus.study.my.web.service.impl;

import org.keyus.study.my.core.annotation.MyService;
import org.keyus.study.my.web.service.UserService;

@MyService("userService")
public class UserServiceImpl implements UserService {

    @Override
    public String query(String name, String password) {
        return "name = " + name + " & password = " + password;
    }
}
