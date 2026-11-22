package com.jss.messaging.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "messages",
    indexes = {
        @Index(name = "idx_conversation_thread", columnList = "job_id, employer_id, job_seeker_id, sent_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "employer_id", nullable = false)
    private Long employerId;

    @Column(name = "job_seeker_id", nullable = false)
    private Long jobSeekerId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType;

    @Column(name = "subject", nullable = true, length = 200)
    private String subject;

    @Column(name = "message_description", nullable = false, length = 3000)
    private String messageDescription;

    @Column(name = "sent_date", nullable = false, updatable = false)
    private LocalDateTime sentDate;

    @PrePersist
    protected void onCreate() {
        if (this.sentDate == null) {
            this.sentDate = LocalDateTime.now();
        }
    }
}
