package com.dore.assistant;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
class AssistantController {

    record AudioData(
            @NotNull @Pattern(regexp = "audio/(webm|ogg|opus|mpeg|mp3|wav|m4a|aac|flac)") String mimeType,
            // Unos 6 MB de audio: de sobra para una nota de voz.
            @NotBlank @Size(max = 8_000_000) String data) {
    }

    record PastTurn(boolean fromUser, @NotBlank @Size(max = 1000) String text) {
    }

    record Message(@Size(max = 1000) String text, @Valid AudioData audio,
            @Size(max = 10) List<@Valid @NotNull PastTurn> history) {
    }

    private final AssistantService assistant;

    AssistantController(AssistantService assistant) {
        this.assistant = assistant;
    }

    @PostMapping("/messages")
    AssistantReply message(@Valid @RequestBody Message message, Principal user) {
        boolean noText = message.text() == null || message.text().isBlank();
        if (noText && message.audio() == null) {
            throw new com.dore.shared.RuleException("Escribe una orden o graba una nota de voz.");
        }
        var audio = message.audio() == null ? null
                : new AssistantModel.Audio(message.audio().mimeType(), message.audio().data());
        var history = message.history() == null ? List.<AssistantModel.Turn>of()
                : message.history().stream().map(t -> new AssistantModel.Turn(t.fromUser(), t.text())).toList();
        return assistant.interpret(user.getName(), message.text(), audio, history);
    }

    @PostMapping("/actions/{id}/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void confirm(@PathVariable String id, Principal user) {
        assistant.confirm(user.getName(), id);
    }

    @DeleteMapping("/actions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(@PathVariable String id, Principal user) {
        assistant.cancel(user.getName(), id);
    }

    @ExceptionHandler(AssistantUnavailableException.class)
    ProblemDetail unavailable(AssistantUnavailableException error) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, error.getMessage());
    }
}
