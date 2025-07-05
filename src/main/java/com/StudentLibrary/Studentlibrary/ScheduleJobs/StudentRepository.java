package com.StudentLibrary.Studentlibrary.ScheduleJobs;

import com.StudentLibrary.Studentlibrary.Model.Student;
import org.springframework.data.repository.Repository;

interface StudentRepository extends Repository<Student, Integer> {
    Student findByStudentId(String studentId);
}
