package com.soecode.lyf.dao;

import com.soecode.lyf.entity.Book;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BookDao {

    /**
     * 通过ID查询单本图书
     *
     * @param id
     * @return
     */
    @Select("SELECT bookid, name, number " +
            " FROM book " +
            " WHERE bookid = #{bookId}")
    Book queryById(long id);

    /**
     * 查询所有图书
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return
     */
    @Select("SELECT bookid, name, number " +
            " FROM book " +
            " ORDER BY bookid " +
            " LIMIT #{offset}, #{limit}")
    @ResultType(Book.class)
    List<Book> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 减少馆藏数量
     *
     * @param bookId
     * @return 如果影响行数等于>1，表示更新的记录行数
     */
    @Update("UPDATE book " +
            " SET number = number - 1 " +
            " WHERE bookid = #{bookId} " +
            " AND number > 0")
    int reduceNumber(long bookId);

}
