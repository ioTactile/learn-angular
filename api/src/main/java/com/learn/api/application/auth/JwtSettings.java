package com.learn.api.application.auth;

/**
 * Settings JWT exposés à la couche application (sans dépendre de Spring Config Properties).
 */
public interface JwtSettings {

	long accessExpirationMinutes();

	long refreshExpirationDays();
}
