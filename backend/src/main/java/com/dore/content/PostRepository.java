package com.dore.content;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class PostRepository {

    private final JdbcClient jdbc;

    PostRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    List<PostView> all() {
        return jdbc.sql("""
                select id, title, platform, status, scheduled_date, reach, interactions, notes
                from content_post order by scheduled_date desc nulls first, id desc
                """).query(PostView.class).list();
    }

    void insert(PostData post) {
        jdbc.sql("""
                insert into content_post (title, platform, status, scheduled_date, reach, interactions, notes)
                values (?,?,?,?,?,?,?)
                """).params(post.title().strip(), post.platform().name(), post.status().name(),
                post.scheduledDate(), post.reach(), post.interactions(), notes(post)).update();
    }

    int update(long id, PostData post) {
        return jdbc.sql("""
                update content_post set title = ?, platform = ?, status = ?, scheduled_date = ?,
                       reach = ?, interactions = ?, notes = ? where id = ?
                """).params(post.title().strip(), post.platform().name(), post.status().name(),
                post.scheduledDate(), post.reach(), post.interactions(), notes(post), id).update();
    }

    int delete(long id) {
        return jdbc.sql("delete from content_post where id = ?").param(id).update();
    }

    private static String notes(PostData post) {
        return post.notes() == null ? "" : post.notes().strip();
    }
}
