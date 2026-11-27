package com.dore.listening;

import java.time.LocalDate;

import com.dore.shared.SalesChannel;

public record MentionView(long id, SalesChannel source, String author, String message,
        Sentiment sentiment, boolean attended, LocalDate mentionDate) {
}
