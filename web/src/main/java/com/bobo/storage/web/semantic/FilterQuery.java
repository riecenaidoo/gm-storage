package com.bobo.storage.web.semantic;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Query parameters for filtering a resource, bound to a model using Spring {@link
 * ModelAttribute @ModelAttribute}.
 *
 * @apiNote Do not include parameters for pagination or sorting. These should be bound using Spring
 *     {@link PageableDefault @PageableDefault} and/or {@link Pageable}/{@link Sort}, which Spring
 *     has a lot of built-in support for.
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Query">URI Query |
 *     MDN</a>
 */
public interface FilterQuery extends RequestSchema {}
