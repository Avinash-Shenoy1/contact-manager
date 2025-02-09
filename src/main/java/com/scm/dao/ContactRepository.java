package com.scm.dao;

import com.scm.entities.Contact;
import com.scm.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    // pagination
    @Query("from Contact as c where c.user.id = :userId")
    // currentPage - page
    //Contact Per Page - 5
    public Page<Contact> findContactByUser(@Param("userId") Integer userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from Contact c where c.cId = :cId")
    public void deleteContactByCId(@Param("cId") Long cId);

    //search
    public List<Contact> findByNameContainingAndUser(String name, User user);
}
