package be.pxl.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService implements IMailService {
    @Autowired
    private JavaMailSender mailSender;
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Override
    public void sendMail(String subject, String text) {
        log.info("Sending mail with subject: " + subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("notermansian@gmail.com");
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
