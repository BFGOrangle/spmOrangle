package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.CreateTagDto;
import com.spmorangle.crm.taskmanagement.dto.TagDto;
import com.spmorangle.crm.taskmanagement.mapper.TagMapper;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.repository.TagRepository;
import com.spmorangle.crm.taskmanagement.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    public TagServiceImpl(TagRepository tagRepository, TagMapper tagMapper){
        this.tagRepository = tagRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    public List<TagDto> getTags() {
        List<Tag> allTags = tagRepository.findAll();
        return allTags.stream()
                .map(tagMapper::toDto)
                .toList();
    }

    @Override
    public TagDto createTag(CreateTagDto createTagDto){
        String tagName = createTagDto.tagName();

        // Check if tag exists (including soft-deleted ones)
        Tag existingTag = tagRepository.findByTagNameIncludingDeleted(tagName).orElse(null);

        if (existingTag != null) {
            if (existingTag.isDeleteInd()) {
                // Reactivate soft-deleted tag
                existingTag.setDeleteInd(false);
                Tag reactivatedTag = tagRepository.save(existingTag);
                log.info("Reactivated soft-deleted tag: {}", tagName);
                return tagMapper.toDto(reactivatedTag);
            }
            log.info("Tag already exists: {}", tagName);
            return tagMapper.toDto(existingTag);
        }

        Tag newTag = new Tag();
        newTag.setTagName(tagName);
        newTag.setDeleteInd(false);
        Tag savedTag = tagRepository.save(newTag);
        log.info("Created new tag: {}", tagName);
        return tagMapper.toDto(savedTag);
    }

    @Override
    @Transactional
    public Tag findOrCreateTag(String tagName) {
        return tagRepository.findActiveByTagName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setTagName(tagName);
                    newTag.setDeleteInd(false);
                    return tagRepository.save(newTag);
                });
    }

    @Override
    @Transactional
    public Set<Tag> findOrCreateTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
                tags.add(findOrCreateTag(tagName));
            }
        }
        return tags;
    }

    @Override
    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        if (tag.isDeleteInd()) {
            log.warn("Tag {} is already deleted", tagId);
            return;
        }

        tag.setDeleteInd(true);
        tagRepository.save(tag);
        log.info("Soft-deleted tag: {} (id: {})", tag.getTagName(), tagId);
    }
}
