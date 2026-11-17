package com.tadak.akai.dto;

import com.tadak.akai.domain.Tag;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** 태그 (F-04) */
public record TagView(Long id, String name) {

    public static TagView from(Tag tag) {
        return new TagView(tag.getId(), tag.getName());
    }

    /** 화면에 늘 같은 순서로 보이도록 이름순으로 정렬한다. */
    public static List<TagView> sortedFrom(Collection<Tag> tags) {
        return tags.stream().map(TagView::from).sorted(Comparator.comparing(TagView::name)).toList();
    }
}
