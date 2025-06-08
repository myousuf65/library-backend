package com.StudentLibrary.Studentlibrary.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking if student_id column exists in student table...");
        
        try {
            // Check if the column exists
            boolean columnExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='student' AND column_name='student_id')",
                Boolean.class
            );
            
            if (!columnExists) {
                System.out.println("Adding student_id column to student table...");
                jdbcTemplate.execute("ALTER TABLE student ADD COLUMN student_id VARCHAR(255)");
                System.out.println("student_id column added successfully!");
            } else {
                System.out.println("student_id column already exists.");
            }
        } catch (Exception e) {
            System.err.println("Error checking/adding student_id column: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
