package com.dore.content;

import java.time.LocalDate;

public record PostView(long id, String title, Platform platform, PostStatus status,
        LocalDate scheduledDate, int reach, int interactions, String notes) {
}
