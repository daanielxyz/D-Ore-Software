package com.dore.content;

import java.util.List;

import com.dore.shared.NotFoundException;
import com.dore.shared.RuleException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Calendario de contenido. No publica en ninguna red. */
@Service
@Transactional
public class ContentService {

    private final PostRepository posts;

    ContentService(PostRepository posts) {
        this.posts = posts;
    }

    public List<PostView> all() {
        return posts.all();
    }

    public void create(PostData post) {
        validate(post);
        posts.insert(post);
    }

    public void update(long id, PostData post) {
        validate(post);
        found(posts.update(id, post), id);
    }

    public void delete(long id) {
        found(posts.delete(id), id);
    }

    private static void validate(PostData post) {
        if (post.status() == PostStatus.SCHEDULED && post.scheduledDate() == null) {
            throw new RuleException("Una publicación programada necesita fecha.");
        }
    }

    private static void found(int rows, long id) {
        if (rows == 0) {
            throw new NotFoundException("No existe la publicación " + id + ".");
        }
    }
}
