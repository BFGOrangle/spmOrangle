package com.spmorangle.crm.taskmanagement.controller;

import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.CreateTagDto;
import com.spmorangle.crm.taskmanagement.dto.TagDto;
import com.spmorangle.crm.taskmanagement.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping()
    public ResponseEntity<List<TagDto>> getTags(){
        List<TagDto> allTags = tagService.getTags();
        return ResponseEntity.status(HttpStatus.FOUND).body(allTags);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping()
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody CreateTagDto createTagDto){
        TagDto createdTag = tagService.createTag(createTagDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
    }
}
