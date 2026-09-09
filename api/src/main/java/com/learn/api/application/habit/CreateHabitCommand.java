package com.learn.api.application.habit;

import java.util.UUID;

public record CreateHabitCommand(UUID ownerId, String title) {
}
