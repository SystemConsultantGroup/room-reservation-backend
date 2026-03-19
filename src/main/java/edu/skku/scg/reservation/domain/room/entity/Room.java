package edu.skku.scg.reservation.domain.room.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer capacity;

    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomAccessPolicy accessPolicy;

    @Builder
    public Room(String name, Integer capacity, String roomNumber, RoomAccessPolicy accessPolicy) {
        this.name = name;
        this.capacity = capacity;
        this.roomNumber = roomNumber;
        this.accessPolicy = accessPolicy;
    }
}