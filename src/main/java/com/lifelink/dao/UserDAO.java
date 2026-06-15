package com.lifelink.dao;

import com.lifelink.model.User;

public interface UserDAO {
    boolean create(User user);
    User readById(int id);
    User readByUsername(String username);
    boolean update(User user);
    boolean delete(int id);
}
