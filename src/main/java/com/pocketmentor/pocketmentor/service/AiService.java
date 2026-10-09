package com.pocketmentor.pocketmentor.service;
import com.pocketmentor.pocketmentor.dto.GeneratedContent;

public interface AiService {
    GeneratedContent generate(String notesText);
}