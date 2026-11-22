package com.jss.messaging.repository;

import com.jss.messaging.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByJobIdAndEmployerIdAndJobSeekerIdOrderBySentDateAsc(Long jobId, Long employerId, Long jobSeekerId);
}
