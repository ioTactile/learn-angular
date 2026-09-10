package com.learn.api.application.habit;

import java.util.UUID;

public record ListHabitsQuery(UUID ownerId, String q, int page, int size) {
}
