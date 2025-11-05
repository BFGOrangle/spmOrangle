package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.CreateTagDto;
import com.spmorangle.crm.taskmanagement.dto.TagDto;
import com.spmorangle.crm.taskmanagement.model.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {
    List<TagDto> getTags();
    TagDto createTag(CreateTagDto createTagDto);
    Tag findOrCreateTag(String tagName);
    Set<Tag> findOrCreateTags(List<String> tagNames);
    void deleteTag(Long tagId);
}
