package org.keyus.study.my.web.dao.impl;

import org.keyus.study.my.core.annotation.MyRepository;
import org.keyus.study.my.web.dao.UserDao;

@MyRepository
public class UserDaoImpl implements UserDao {

    @Override
    public String query(String name, String password) {
        return "name = " + name + " & password = " + password;
    }
}
