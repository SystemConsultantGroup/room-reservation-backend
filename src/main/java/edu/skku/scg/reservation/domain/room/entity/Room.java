package edu.skku.scg.reservation.domain.room.entity;

import edu.skku.scg.reservation.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

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

    private Integer maxBookingMinutes;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MajorRoom> majorRooms = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomOperatingHour> operatingHours = new ArrayList<>();

    @Builder
    public Room(String name, Integer capacity, String roomNumber, RoomAccessPolicy accessPolicy, Integer maxBookingMinutes) {
        this.name = name;
        this.capacity = capacity;
        this.roomNumber = roomNumber;
        this.accessPolicy = accessPolicy;
        this.maxBookingMinutes = maxBookingMinutes;
    }

    public void update(String name, Integer capacity, String roomNumber, RoomAccessPolicy accessPolicy, Integer maxBookingMinutes) {
        this.name = name;
        this.capacity = capacity;
        this.roomNumber = roomNumber;
        this.accessPolicy = accessPolicy;
        this.maxBookingMinutes = maxBookingMinutes;
    }
}