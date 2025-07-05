package com.StudentLibrary.Studentlibrary.ScheduleJobs;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OverdueSchedulerTest {

    @Autowired
    OverdueScheduler scheduler;



    @Test
    void sendTestEmail(){
        scheduler.sendEmail(
                "feeds015@gmail.com",
                "Test Subject",
                "Test Content"
        );
    }


}