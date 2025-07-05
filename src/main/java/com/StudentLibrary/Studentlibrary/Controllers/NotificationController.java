package com.StudentLibrary.Studentlibrary.Controllers;

import com.StudentLibrary.Studentlibrary.Model.Notifications;
import com.StudentLibrary.Studentlibrary.Model.Student;
import com.StudentLibrary.Studentlibrary.Repositories.NotificationRepository;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;

    public NotificationController(NotificationRepository notificationRepository, StudentRepository studentRepository) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/notifications/{student_id}")
    ResponseEntity<?> getNotificationForStudent(@PathVariable String student_id){
        Student student = studentRepository.findByStudentId(student_id);
        List<Notifications> allNotifications = notificationRepository.findByStudent(student);

        return ResponseEntity.ok(allNotifications);
    }

    @GetMapping("/notifications/unread/{student_id}")
    ResponseEntity<?> getUnreadNotificationForStudent(@PathVariable String student_id){
        Student student = studentRepository.findByStudentId(student_id);

        List<Notifications> allNotifications = notificationRepository.findByStudentAndRead(student,false);

        return ResponseEntity.ok(allNotifications);
    }


    @PostMapping("/notifications/markasread/{student_id}")
    ResponseEntity<?> markAsRead(@PathVariable String student_id, @RequestBody JsonNode payload){
        return null;
    }

}
