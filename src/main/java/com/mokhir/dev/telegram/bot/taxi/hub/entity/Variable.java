package com.mokhir.dev.telegram.bot.taxi.hub.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "variables")
public class Variable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private Integer messageId;
    private String variableName;
    private Integer variableValue;
}
