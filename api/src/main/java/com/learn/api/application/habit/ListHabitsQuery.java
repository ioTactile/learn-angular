package com.learn.api.application.habit;

import java.util.UUID;

public record ListHabitsQuery(UUID ownerId, UUID workspaceId, String q, int page, int size) {
}
