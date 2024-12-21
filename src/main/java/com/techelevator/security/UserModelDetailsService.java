package com.techelevator.security;

import com.techelevator.dao.UserDao;
import com.techelevator.exception.UserNotActivatedException;
import com.techelevator.model.Authority;
import com.techelevator.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class UserModelDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserModelDetailsService.class);

    private final UserDao userDao;

    public UserModelDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating user '{}'", login);
        User user = userDao.getUserByUsername(login);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + login);
        }
        return createSpringSecurityUser(login, user);
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {
        // Check if the user is activated based on userType
        if (user.getUserType() == null) {
            throw new UserNotActivatedException("User " + login + " was not activated");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        Set<Authority> userAuthorities = user.getAuthorities();
        for (Authority authority : userAuthorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPasswordHash(),
                grantedAuthorities);
    }
}