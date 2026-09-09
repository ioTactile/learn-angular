package com.learn.api.application.auth;

/**
 * Commande d'entrée du use case (pas de annotations HTTP ici).
 */
public record RegisterUserCommand(String email, String password) {
}
