package com.digital5.repository;

import com.digital5.entity.VerifyUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerifyUserRepository extends JpaRepository <VerifyUserEntity, UUID> {}
