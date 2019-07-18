package com.vinberts.shrinkly.service;

/**
 *
 */
public interface ShrinklyUrlHashService {

    String generateHash(String url);

    String generateRandomHash(Integer length);

}
