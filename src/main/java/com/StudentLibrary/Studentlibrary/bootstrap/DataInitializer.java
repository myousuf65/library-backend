package com.StudentLibrary.Studentlibrary.bootstrap;

import com.StudentLibrary.Studentlibrary.Model.User;
import com.StudentLibrary.Studentlibrary.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user if it doesn't exist
        if (!userService.existsByUsername("admin@library.com")) {
            User adminUser = new User();
            adminUser.setUsername("admin@library.com");
            // Don't encode the password here, the service will do it
            adminUser.setPassword("admin123");
            adminUser.setRoles(new ArrayList<>(Arrays.asList("ADMIN")));
            User savedUser = userService.createUser(adminUser);
            System.out.println("Admin user created with username: admin@library.com");
            System.out.println("Admin user ID: " + savedUser.getId());
        } else {
            System.out.println("Admin user already exists");
            // For debugging, let's update the password to make sure it's correct
            User adminUser = userService.findByUsername("admin@library.com");
            adminUser.setPassword("admin123");
            userService.updateUser(adminUser);
            System.out.println("Admin user password updated");
        }
    }
}
