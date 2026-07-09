package com.digital5.repository;

import com.digital5.entity.PublicKeysEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicKeysRepository extends JpaRepository<PublicKeysEntity, String> {

}
