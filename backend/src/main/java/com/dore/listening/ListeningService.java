package com.dore.listening;

import java.time.LocalDate;
import java.util.List;

import com.dore.shared.BusinessCalendar;
import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Menciones anotadas a mano. No rastrea redes ni responde. */
@Service
@Transactional
public class ListeningService {

    private final MentionRepository mentions;

    ListeningService(MentionRepository mentions) {
        this.mentions = mentions;
    }

    public List<MentionView> recent() {
        return mentions.recent();
    }

    public void register(NewMention mention) {
        LocalDate date = mention.mentionDate() == null ? BusinessCalendar.today() : mention.mentionDate();
        if (date.isAfter(BusinessCalendar.today())) {
            throw new RuleException("La fecha de la mención no puede ser futura.");
        }
        mentions.insert(mention, date);
    }

    public void setAttended(long id, boolean attended) {
        found(mentions.setAttended(id, attended), id);
    }

    public void delete(long id) {
        found(mentions.delete(id), id);
    }

    private static void found(int rows, long id) {
        if (rows == 0) {
            throw new NotFoundException("No existe la mención " + id + ".");
        }
    }
}
