package com.tourai.develop.repository;

import com.tourai.develop.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, UserCustomRepository {

    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String username);

    @Query("""
                select distinct u
                from User u
                left join fetch u.userTags ut
                left join fetch ut.tag t
                where u.id = :userId
            """)
    Optional<User> findByIdWithTags(@Param("userId")Long userId);

}
