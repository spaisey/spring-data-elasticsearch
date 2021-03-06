/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core.convert;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.convert.EntityConverter;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.document.SearchDocument;
import org.springframework.data.elasticsearch.core.document.SearchDocumentResponse;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 * @author Sasch Woo
 */
public interface ElasticsearchConverter
		extends EntityConverter<ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty, Object, Document> {

	/**
	 * Get the configured {@link ProjectionFactory}. <br />
	 * <strong>NOTE</strong> Should be overwritten in implementation to make use of the type cache.
	 *
	 * @since 3.2
	 */
	default ProjectionFactory getProjectionFactory() {
		return new SpelAwareProxyProjectionFactory();
	}

	// region read
	/**
	 * Map a single {@link Document} to an instance of the given type.
	 *
	 * @param document the document to map
	 * @param type must not be {@literal null}.
	 * @param <T> the class of type
	 * @return can be {@literal null} if the document is null or {@link Document#isEmpty()} is true.
	 * @since 4.0
	 */
	@Nullable
	<T> T mapDocument(@Nullable Document document, Class<T> type);

	/**
	 * Map a list of {@link Document}s to a list of instance of the given type.
	 *
	 * @param documents must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @param <T> the class of type
	 * @return a list obtained by calling {@link #mapDocument(Document, Class)} on the elements of the list.
	 * @since 4.0
	 */
	default <T> List<T> mapDocuments(List<Document> documents, Class<T> type) {
		return documents.stream().map(document -> mapDocument(document, type)).collect(Collectors.toList());
	}

	/**
	 * builds a {@link SearchHits} from a {@link SearchDocumentResponse}.
	 * 
	 * @param <T> the clazz of the type, must not be {@literal null}.
	 * @param type the type of the returned data, must not be {@literal null}.
	 * @param searchDocumentResponse the response to read from, must not be {@literal null}.
	 * @return a SearchHits object
	 * @since 4.0
	 */
	<T> SearchHits<T> read(Class<T> type, SearchDocumentResponse searchDocumentResponse);
	
	/**
	 * builds a {@link SearchScrollHits} from a {@link SearchDocumentResponse}.
	 * 
	 * @param <T> the clazz of the type, must not be {@literal null}.
	 * @param type the type of the returned data, must not be {@literal null}.
	 * @param searchDocumentResponse the response to read from, must not be {@literal null}.
	 * @return a {@link SearchScrollHits} object
	 * @since 4.0
	 */
	<T> SearchScrollHits<T> readScroll(Class<T> type, SearchDocumentResponse searchDocumentResponse);

	/**
	 * builds a {@link SearchHit} from a {@link SearchDocument}.
	 *
	 * @param <T> the clazz of the type, must not be {@literal null}.
	 * @param type the type of the returned data, must not be {@literal null}.
	 * @param searchDocument must not be {@literal null}
	 * @return SearchHit with all available information filled in
	 * @since 4.0
	 */
	<T> SearchHit<T> read(Class<T> type, SearchDocument searchDocument);

	// endregion

	// region write
	/**
	 * Convert a given {@literal idValue} to its {@link String} representation taking potentially registered
	 * {@link org.springframework.core.convert.converter.Converter Converters} into account.
	 *
	 * @param idValue must not be {@literal null}.
	 * @return never {@literal null}.
	 * @since 3.2
	 */
	default String convertId(Object idValue) {

		Assert.notNull(idValue, "idValue must not be null!");

		if (!getConversionService().canConvert(idValue.getClass(), String.class)) {
			return idValue.toString();
		}

		return getConversionService().convert(idValue, String.class);
	}

	/**
	 * Map an object to a {@link Document}.
	 *
	 * @param source the object to map
	 * @return will not be {@literal null}.
	 */
	default Document mapObject(@Nullable Object source) {
		Document target = Document.create();
		write(source, target);
		return target;
	}
	// endregion

	/**
	 * Updates a query by renaming the property names in the query to the correct mapped field names and the values to the
	 * converted values if the {@link ElasticsearchPersistentProperty} for a property has a
	 * {@link org.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentPropertyConverter}.
	 * 
	 * @param criteriaQuery the query that is internally updated
	 * @param domainClass the class of the object that is searched with the query
	 */
	// region query
	void updateQuery(CriteriaQuery criteriaQuery, Class<?> domainClass);
	// endregion
}
