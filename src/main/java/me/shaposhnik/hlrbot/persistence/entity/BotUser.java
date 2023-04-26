package me.shaposhnik.hlrbot.persistence.entity;

import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.shaposhnik.hlrbot.model.enums.UserState;
import org.hibernate.annotations.DynamicUpdate;

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
