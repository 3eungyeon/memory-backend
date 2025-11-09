package yunhan.supplement.mapper;

//package com.example.demo.mapper;
//
//import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import yunhan.supplement.Entity.UserEntity;

import java.util.List;

@Mapper
public interface FriendMapper {

    @Select("SELECT u.id, u.username, u.name " +
            "FROM friendship f " +
            "JOIN friendship sf ON f.receiver_id = sf.sender_id AND f.sender_id = sf.receiver_id " +
            "JOIN users u ON (sf.sender_id = u.id OR sf.receiver_id = u.id) " +
            "WHERE f.sender_id = #{userId} " +
            "AND f.is_friend = TRUE " +
            "AND sf.is_friend = TRUE " +
            "AND u.id != #{userId}")
//    @Select("SELECT u.id, u.username, u.name " +
//            "FROM friendship f " +
//            "JOIN users u ON (f.sender_id = u.id OR f.receiver_id = u.id) " +
//            "WHERE (f.sender_id = #{userId} OR f.receiver_id = #{userId}) " +
//            "AND f.is_friend = TRUE " +
//            "AND u.id != #{userId}")
    List<UserEntity> findFriendsByUserId(@Param("userId") int userId);
}