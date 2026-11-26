package com.dore.listening;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentions")
class ListeningController {

    private final ListeningService listening;

    ListeningController(ListeningService listening) {
        this.listening = listening;
    }

    record Attended(boolean attended) {
    }

    @GetMapping
    List<MentionView> recent() {
        return listening.recent();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void register(@Valid @RequestBody NewMention mention) {
        listening.register(mention);
    }

    @PutMapping("/{id}/attended")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setAttended(@PathVariable long id, @RequestBody Attended body) {
        listening.setAttended(id, body.attended());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable long id) {
        listening.delete(id);
    }
}
