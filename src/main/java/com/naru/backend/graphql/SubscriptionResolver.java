package com.naru.backend.graphql;

import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import com.naru.backend.model.Post;

import graphql.kickstart.tools.GraphQLSubscriptionResolver;

@Component
public class SubscriptionResolver implements GraphQLSubscriptionResolver {
    private final GraphQLPublisher publisher;

    public SubscriptionResolver(GraphQLPublisher publisher) {
        this.publisher = publisher;
    }

    public Publisher<Post> postCreated() {
        return publisher.getPublisher("postCreated", Post.class);
    }
}