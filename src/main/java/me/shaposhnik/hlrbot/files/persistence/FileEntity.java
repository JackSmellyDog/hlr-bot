package me.shaposhnik.hlrbot.files.persistence;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.nio.file.Path;

@Entity
@Table(name = "files")
@DynamicUpdate
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String fullPath;

    @Column(nullable = false)
    private String realFileName;

    @Column(nullable = false)
    private String receivedFileName;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column
    private String mimeType;

    public Path toPath() {
        return Path.of(fullPath);
    }

    public File toFile() {
        return toPath().toFile();
    }

}
