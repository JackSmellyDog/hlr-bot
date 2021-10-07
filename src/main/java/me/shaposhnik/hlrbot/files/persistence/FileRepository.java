package me.shaposhnik.hlrbot.files.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, String> {

    Optional<FileEntity> findByIdAndDeletedFalse(String id);

    @Modifying
    @Query("update FileEntity f set f.deleted = true where f.id = :id")
    void markDeleted(@Param("id") String id);
}
