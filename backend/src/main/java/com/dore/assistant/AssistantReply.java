package com.dore.assistant;

import java.util.List;

/** proposal es nulo cuando no hay nada que confirmar. heard: lo que entendió del audio. */
public record AssistantReply(String message, String heard, Proposal proposal) {

    public record Proposal(String id, List<Action.Preview> actions) {
    }
}
