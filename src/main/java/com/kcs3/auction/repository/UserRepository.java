package com.kcs3.auction.repository;

import com.kcs3.auction.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

