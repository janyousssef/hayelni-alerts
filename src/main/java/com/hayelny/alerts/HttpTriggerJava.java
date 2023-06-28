package com.hayelny.alerts;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class HttpTriggerJava {
    Email from = new Email("janyoussef6@gmail.com");
    String subject = "HAYELNY URGENT";
    Email[] recipients = {new Email("janyoussef6@gmail.com"), new Email("janyoussef19@gmail.com")};
    Mail[] mails = new Mail[recipients.length];
    SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

    @FunctionName("ping")
    public HttpResponseMessage ping(
            @HttpTrigger(name = "req",
                         methods = {HttpMethod.GET},
                         authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request) {
        return request.createResponseBuilder(HttpStatus.OK).body("pinged at " + LocalTime.now()).build();
    }

    @FunctionName("alert")
    public void alert(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> req) {

        Content content = new Content("text/plain", "Someone has attempted to run hayelny without permission at " +
                LocalDateTime.now().atOffset(ZoneOffset.ofHours(3)));
        for (int i = 0; i < recipients.length; i++) {
            mails[i] = new Mail(from, subject, recipients[i], content);
        }

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        try {
            for (Mail mail : mails) {
                request.setBody(mail.build());
                sg.api(request);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
