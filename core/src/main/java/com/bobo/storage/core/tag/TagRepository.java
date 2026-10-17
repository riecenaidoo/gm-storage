package com.bobo.storage.core.tag;

import com.bobo.storage.core.semantic.EntityRepository;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface TagRepository extends EntityRepository<Tag, Integer>, CrudRepository<Tag, Integer> {

  Optional<Tag> findByTag(String tag);
}
