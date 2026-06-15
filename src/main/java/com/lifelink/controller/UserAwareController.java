package com.lifelink.controller;

import com.lifelink.model.User;

/**
 * Interface for controllers that need to receive a User object after login navigation.
 * Any dashboard controller should implement this interface.
 */
public interface UserAwareController {
    void initializeUser(User user);
}
