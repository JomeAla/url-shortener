package com.jomea.urlshortener.controller;

import com.jomea.urlshortener.entity.Ticket;
import com.jomea.urlshortener.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/api/tickets")
    public ResponseEntity<?> createTicket(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            String email = (String) body.get("email");
            String subject = (String) body.get("subject");
            String message = (String) body.get("message");
            if (name == null || name.isBlank() || email == null || email.isBlank()
                || subject == null || subject.isBlank() || message == null || message.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }
            Ticket t = new Ticket();
            t.setName(name.trim());
            t.setEmail(email.trim());
            t.setSubject(subject.trim());
            t.setMessage(message.trim());
            t.setStatus("OPEN");
            t.setCreatedAt(LocalDateTime.now());
            ticketRepository.save(t);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Ticket submitted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/admin/tickets")
    public ResponseEntity<List<Ticket>> listTickets() {
        return ResponseEntity.ok(ticketRepository.findAllByOrderByCreatedAtDesc());
    }

    @PutMapping("/api/admin/tickets/{id}/reply")
    public ResponseEntity<?> replyTicket(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Ticket t = ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            String reply = (String) body.get("adminReply");
            if (reply != null) {
                t.setAdminReply(reply);
                t.setStatus("CLOSED");
            }
            t.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(t);
            return ResponseEntity.ok(Map.of("message", "Reply saved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/tickets/{id}/status")
    public ResponseEntity<?> updateTicketStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Ticket t = ticketRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            String status = (String) body.get("status");
            if (status != null) t.setStatus(status);
            t.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(t);
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            ticketRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Ticket deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
