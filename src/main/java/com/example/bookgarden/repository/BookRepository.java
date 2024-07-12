package com.example.bookgarden.repository;

import com.example.bookgarden.entity.Book;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<Book, ObjectId> {
    List<Book> findByIsDeletedFalse();
    List<Book> findByIsDeletedTrue();
    @Query("{ '$and' : [{ 'authors' : { '$in' : ?0 }}, { 'categories' : { '$in' : ?1 }}] }")
    List<Book> findRelatedBooksByAuthorsAndCategories(List<ObjectId> authorIds, List<ObjectId> categoryIds);

    @Query("{ 'authors' : { '$in' : ?0 } }")
    List<Book> findRelatedBooksByAuthors(List<ObjectId> authorIds);

    @Query("{ 'categories' : { '$in' : ?0 } }")
    List<Book> findRelatedBooksByCategories(List<ObjectId> categoryIds);
    List<Book> findByIdIn(List<ObjectId> objectIds);
    List<Book> findTop10BySoldQuantityIsNotNullOrderBySoldQuantityDesc();
    List<Book> findByTitleContainingIgnoreCase(String searchQuery);
    List<Book> findByCategoriesContains(ObjectId categoryId);

    List<Book> findByAuthorsContains(ObjectId authorId);
    List<Book> findTop10ByOrderBySoldQuantityDesc();

}
