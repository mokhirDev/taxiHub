package com.mokhir.dev.telegram.bot.taxi.hub.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "current_page_code")
    private String currentPageCode;
    @Column(name = "contact_number")
    private String contactNumber;
    @Column(name = "leaving_date")
    private String  leavingDate;
    @Column(name = "from_city")
    private String from_city;
    @Column(name = "to_city")
    private String to_city;
    @Column(name = "passengers_count")
    private Integer passengersCount;
    @Column(name = "latitude")
    private Integer latitude;
    @Column(name = "longitude")
    private Integer longitude;
}
