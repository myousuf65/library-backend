package com.StudentLibrary.Studentlibrary.ScheduleJobs;


import com.StudentLibrary.Studentlibrary.Model.Transaction;
import com.StudentLibrary.Studentlibrary.Repositories.StudentRepository;
import com.StudentLibrary.Studentlibrary.Repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.stream;

@Component
public class OverdueScheduler {

    @Autowired
    TransactionRepository transactionRepository;


    @Autowired
    JavaMailSender javaMailSender;

    public void sendEmail(String receiver, String subject, String content){
        try{
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(receiver);
            mail.setFrom("work.myousuf@gmail.com");
            mail.setSubject(subject);
            mail.setText(content);
            javaMailSender.send(mail);
        } catch (Exception e) {
            throw new RuntimeException("Error sending mail:" + e);
        }
    }


    @Scheduled(cron = "0 0 12 * * *")
    public void SendReminderEmailForDeadlineApproaching(){
        List<Transaction> allTransaction = transactionRepository.findAll();

        Boolean issue;
        Date tDate;
        for(Transaction t : allTransaction){
            issue = t.getIsIssueOperation();
            tDate = t.getTransactionDate();

            Date today = new Date();
            long diffInMillies = Math.abs(today.getTime() - tDate.getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if(issue.equals(false) && diffInDays >= 6 && diffInDays <= 10  ){
                // send an email to them saying your deadline is approaching please return the book
                sendEmail(
                        t.getStudent().getEmailId(),
                        "Please return book ASAP",
                        "Your deadline is approaching in less than 4 days"
                );
            }

            if(issue.equals(false) && diffInDays >= 10 && diffInDays <= 12  ){
                // send an email to warn them that they will be fined
                sendEmail(
                        t.getStudent().getEmailId(),
                        "You have exceded the deadline",
                        "Please return book ASAP else you will be fined"
                );
            }

            if(issue.equals(false) && diffInDays > 12 ){
                // send an email to tell them about their fine
                sendEmail(
                        t.getStudent().getEmailId(),
                        "You have been fined",
                        "You have been fined for not returning book on time. Please visit the Library Webpage to pay the fine"
                );
            }

        }
    }


}
