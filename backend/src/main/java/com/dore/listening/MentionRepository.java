package com.dore.listening;

import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class MentionRepository {

    private final JdbcClient jdbc;

    MentionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<MentionView> recent() {
        return jdbc.sql("""
                select id, source, author, message, sentiment, attended, mention_date
                from mention order by attended, mention_date desc, id desc limit 200
                """).query(MentionView.class).list();
    }

    void insert(NewMention mention, LocalDate date) {
        jdbc.sql("insert into mention (source, author, message, sentiment, mention_date) values (?,?,?,?,?)")
                .params(mention.source().name(), mention.author().strip(), mention.message().strip(),
                        mention.sentiment().name(), date)
                .update();
    }

    int setAttended(long id, boolean attended) {
        return jdbc.sql("update mention set attended = ? where id = ?").params(attended, id).update();
    }

    int delete(long id) {
        return jdbc.sql("delete from mention where id = ?").param(id).update();
    }
}
