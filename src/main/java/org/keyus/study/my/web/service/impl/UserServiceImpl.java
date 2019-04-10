package org.keyus.study.my.web.service.impl;

import org.keyus.study.my.core.annotation.MyAutowired;
import org.keyus.study.my.core.annotation.MyService;
import org.keyus.study.my.web.dao.UserDao;
import org.keyus.study.my.web.service.UserService;

@MyService("userService")
public class UserServiceImpl implements UserService {

    @MyAutowired
    private UserDao userDao;

    @Override
    public String query(String name, String password) {
        return userDao.query(name, password);
    }
}
