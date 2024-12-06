package com.naru.backend.graphql;

import org.springframework.stereotype.Component;

import com.naru.backend.model.Post;

import graphql.kickstart.tools.GraphQLMutationResolver;

@Component
public class MutationResolver implements GraphQLMutationResolver {
    private final GraphQLPublisher publisher;

    public MutationResolver(GraphQLPublisher publisher) {
        this.publisher = publisher;
    }

    public void createPost(Post post) {
        // 새로 생성된 Post를 Subscription으로 전송
        publisher.publish("postCreated", post);
    }
}