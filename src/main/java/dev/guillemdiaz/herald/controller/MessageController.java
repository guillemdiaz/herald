package dev.guillemdiaz.herald.controller;

import dev.guillemdiaz.herald.dto.MessageRequest;
import dev.guillemdiaz.herald.dto.MessageResponse;
import dev.guillemdiaz.herald.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Endpoints for sending SMS and viewing " +
        "tenant-isolated message history")
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "Dispatch a new message", description = "Sends an " +
            "SMS. Rate limited to 5 requests per minute.")
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> send(
            @Valid @RequestBody MessageRequest request,
            Principal principal
    ) {
        String email = principal.getName();
        log.info("Sending message to {} for tenant {}",
                request.recipientNumber(), email);
        MessageResponse response = messageService.sendMessage(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "View message history", description = "Returns a " +
            "list of all messages sent by the authenticated tenant.")
    @GetMapping("/history")
    public ResponseEntity<List<MessageResponse>> getHistory(Principal principal) {
        String email = principal.getName();
        log.info("Fetching message history for Tenant: {}", email);
        List<MessageResponse> history =
                messageService.getMessageHistory(email);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Fetch a single message", description = "Retrieves a" +
            " specific message by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable @Min(1) Long id,
                                            Principal principal) {
        String email = principal.getName();
        log.info("Fetching message ID: {} for tenant {}", id, email);
        MessageResponse response = messageService.getMessageById(email, id);
        return ResponseEntity.ok(response);
    }
}