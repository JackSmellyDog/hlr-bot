package me.shaposhnik.hlrbot.persistence.entity;

import lombok.*;
import me.shaposhnik.hlrbot.model.enums.UserState;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Locale;

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
    private Long id;

    @Column
    @Enumerated(EnumType.STRING)
    private UserState state = UserState.NEW;

    @Column
    private String apiKey;

    @Column
    private String userName;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column(name = "language_code")
    @Convert(converter = LocaleAttributeConverter.class)
    private Locale locale;

}
