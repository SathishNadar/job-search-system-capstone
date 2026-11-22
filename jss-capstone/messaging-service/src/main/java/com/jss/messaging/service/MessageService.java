package com.jss.messaging.service;

import com.jss.messaging.dto.MessageRequestDTO;
import com.jss.messaging.dto.MessageResponseDTO;

import java.util.List;

public interface MessageService {

    MessageResponseDTO sendMessage(MessageRequestDTO requestDTO);

    List<MessageResponseDTO> getConversation(Long jobId, Long employerId, Long jobSeekerId);
}
