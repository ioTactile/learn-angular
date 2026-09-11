package com.learn.api.application.habit;

import java.util.UUID;

public record CreateHabitCommand(UUID ownerId, UUID workspaceId, String title) {
}
