package me.shaposhnik.hlrbot.persistence.entity;

import lombok.*;
import me.shaposhnik.hlrbot.model.enums.UserState;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Table(name = "bot_user")
@DynamicUpdate
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotUser {

    @Id
    @Column(unique = true, nullable = false)
    private Long telegramId;

    @Column
    @Enumerated(EnumType.STRING)
    private UserState state = UserState.NEW;

    @Column
    private String apiKey;

    @Column(nullable = false)
    private String userName;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String languageCode;

}
