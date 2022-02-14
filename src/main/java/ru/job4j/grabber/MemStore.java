package ru.job4j.grabber;

import ru.job4j.model.Post;

import java.util.ArrayList;
import java.util.List;

public class MemStore implements Store {
    private List<Post> posts = new ArrayList<>();

    @Override
    public void save(Post post) {
        post.setId(posts.size() + 1);
        posts.add(post);
    }

    @Override
    public List<Post> getAll() {
        return posts;
    }

    @Override
    public Post findById(int id) {
        return posts.stream()
                .filter(post -> post.getId() == id).findFirst().orElse(null);
    }
}
