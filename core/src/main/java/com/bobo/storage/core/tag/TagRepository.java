package com.bobo.storage.core.tag;

import com.bobo.storage.core.semantic.EntityRepository;
import org.springframework.data.repository.CrudRepository;

/**
 * @implSpec {@link EntityRepository}
 */
public interface TagRepository
    extends EntityRepository<Tag, Integer>, CrudRepository<Tag, Integer> {}
