package com.scm.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Properties;

@Service
public class EmailService {

    String from = "ramakrishna1.ammu@gmail.com";
    String password = "guyi kriv opdt buig";

//    //this is responsible to send the message with attachment
//    public boolean sendAttach(String message, String subject, String to) {
//
//        boolean result = false;
//
//        //Variable for gmail
//        String host="smtp.gmail.com";
//
//        //get the system properties
//        Properties properties = System.getProperties();
//        System.out.println("PROPERTIES "+properties);
//
//        //setting important information to properties object
//
//        //host set
//        properties.put("mail.smtp.host", host);
//        properties.put("mail.smtp.port","465");
//        properties.put("mail.smtp.ssl.enable","true");
//        properties.put("mail.smtp.auth","true");
//
//        //Step 1: to get the session object..
//        Session session=Session.getInstance(properties, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(from, password);
//            }
//        });
//
//        session.setDebug(true);
//
//        //Step 2 : compose the message [text,multi media]
//        MimeMessage m = new MimeMessage(session);
//
//        try {
//
//            //from email
//            m.setFrom(from);
//
//            //adding recipient to message
//            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
//
//            //adding subject to message
//            m.setSubject(subject);
//
//
//            //attachement..
//
//            //file path
//            String path="/Users/avinashshenoy/Downloads/contact-imgage.jpeg";
//
//
//            MimeMultipart mimeMultipart = new MimeMultipart();
//            //text
//            //file
//
//            MimeBodyPart textMime = new MimeBodyPart();
//
//            MimeBodyPart fileMime = new MimeBodyPart();
//
//            try {
//
//                textMime.setText(message);
//
//                File file=new File(path);
//                fileMime.attachFile(file);
//
//
//                mimeMultipart.addBodyPart(textMime);
//                mimeMultipart.addBodyPart(fileMime);
//
//
//            } catch (Exception e) {
//
//                e.printStackTrace();
//            }
//
//
//            m.setContent(mimeMultipart);
//
//
//            //send
//
//            //Step 3 : send the message using Transport class
//            Transport.send(m);
//            result = true;
//
//
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println("Sent success...................");
//        return result;
//
//
//    }

    //this is responsible to send email..
    public boolean sendEmail(String message, String subject, String to) {

        boolean result = false;

        //Variable for gmail
        String host = "smtp.gmail.com";

        //get the system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES " + properties);

        //setting important information to properties object

        //host set
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        //Step 1: to get the session object..
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }


        });

        session.setDebug(true);

        //Step 2 : compose the message [text,multi media]
        MimeMessage m = new MimeMessage(session);

        try {

            //from email
            m.setFrom(from);

            //adding recipient to message
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            //adding subject to message
            m.setSubject(subject);


            //adding text to message
            m.setText(message);

            //send

            //Step 3 : send the message using Transport class
            Transport.send(m);

            System.out.println("Sent success...................");
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
