package com.bobo.storage.core.tag.song;

import com.bobo.storage.core.semantic.EntityRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @implSpec {@link EntityRepository}
 */
public interface SongTagRepository
    extends EntityRepository<SongTag, Integer>, CrudRepository<SongTag, Integer> {}
