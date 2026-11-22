package com.jss.messaging.config;

import com.jss.messaging.entity.Message;
import com.jss.messaging.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds a sample two-way conversation thread if MySQL 'messages' table is empty on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final MessageRepository messageRepository;

    @Override
    public void run(String... args) {
        if (messageRepository.count() == 0) {
            log.info("Seeding initial conversation messages into MySQL ('messaging_db')...");

            Message msg1 = Message.builder()
                    .employerId(1L)
                    .jobSeekerId(1L)
                    .jobId(1L)
                    .senderType("EMPLOYER")
                    .subject("Technical Interview Invitation - Senior Java Developer")
                    .messageDescription("Hi Sathish, we reviewed your application and would like to schedule Technical Round 1 this Thursday at 11:00 AM IST.")
                    .sentDate(LocalDateTime.now().minusHours(5))
                    .build();

            Message msg2 = Message.builder()
                    .employerId(1L)
                    .jobSeekerId(1L)
                    .jobId(1L)
                    .senderType("JOB_SEEKER")
                    .subject("Re: Technical Interview Invitation - Senior Java Developer")
                    .messageDescription("Thank you for the opportunity! I confirm my availability for Thursday at 11:00 AM IST.")
                    .sentDate(LocalDateTime.now().minusHours(2))
                    .build();

            messageRepository.save(msg1);
            messageRepository.save(msg2);

            log.info("Successfully seeded 2 conversation messages.");
        } else {
            log.info("Messages table already contains data. Skipping seeding.");
        }
    }
}
