package com.jss.messaging.controller;

import com.jss.messaging.dto.MessageRequestDTO;
import com.jss.messaging.dto.MessageResponseDTO;
import com.jss.messaging.exception.ErrorResponse;
import com.jss.messaging.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messaging Management", description = "Endpoints for sending messages and viewing job-specific employer-candidate conversation threads")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Send a message", description = "Employer or Job Seeker dispatches a message for a job opening. Validates employerId, jobSeekerId, and jobId via OpenFeign.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message successfully sent",
                    content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (missing fields or invalid senderType)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Employer, Job Seeker, or Job ID not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @Valid @RequestBody MessageRequestDTO requestDTO) {
        MessageResponseDTO saved = messageService.sendMessage(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "View job-specific conversation thread", description = "Retrieves the full chronological conversation between an Employer and Job Seeker for a specific Job Opening. Returns an empty list if no messages exist yet.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation thread retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Missing required query parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/conversation")
    public ResponseEntity<List<MessageResponseDTO>> getConversation(
        @RequestParam Long jobId,
        @RequestParam Long employerId,
        @RequestParam Long jobSeekerId) {
        return ResponseEntity.ok(messageService.getConversation(jobId, employerId, jobSeekerId));
    }
}
