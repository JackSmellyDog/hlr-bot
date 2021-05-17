package me.shaposhnik.hlrbot.persistence.entity;

import lombok.*;
import me.shaposhnik.hlrbot.model.enums.Ported;
import me.shaposhnik.hlrbot.model.enums.Roaming;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "hlr")
@DynamicUpdate
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HlrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String providerId;

    @Column
    private String number;

    @Column
    private String network;

    @Column
    private String status;

    @Column
    @Enumerated(EnumType.STRING)
    private Ported ported;

    @Column
    @Enumerated(EnumType.STRING)
    private Roaming roaming;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime statusReceivedAt;

    @ManyToOne
    @JoinColumn(name = "bot_user")
    private BotUser botUser;

}
