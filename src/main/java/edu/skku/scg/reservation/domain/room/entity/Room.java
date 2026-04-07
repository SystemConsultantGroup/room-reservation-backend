package edu.skku.scg.reservation.domain.room.entity;

import edu.skku.scg.reservation.domain.reservation.entity.Reservation;
import edu.skku.scg.reservation.domain.room.dto.RoomUpdateRequest;
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

    @Column(nullable = false)
    private Integer minAttendeeCount;

    @Column(nullable = false)
    private Integer maxAttendeeCount;

    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomAccessPolicy accessPolicy;

    @Column(nullable = false)
    private Integer minUsageMinutes;

    @Column(nullable = false)
    private Integer maxUsageMinutes;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MajorRoom> majorRooms = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomOperatingHour> operatingHours = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations = new ArrayList<>();

    @Builder
    public Room(String name, Integer minAttendeeCount, Integer maxAttendeeCount, String roomNumber, RoomAccessPolicy accessPolicy, Integer minUsageMinutes, Integer maxUsageMinutes) {
        this.name = name;
        this.minAttendeeCount = minAttendeeCount;
        this.maxAttendeeCount = maxAttendeeCount;
        this.roomNumber = roomNumber;
        this.accessPolicy = accessPolicy;
        this.minUsageMinutes = minUsageMinutes;
        this.maxUsageMinutes = maxUsageMinutes;
    }

    public void update(RoomUpdateRequest command) {
        this.name = command.name();
        this.minAttendeeCount = command.minAttendeeCount();
        this.maxAttendeeCount = command.maxAttendeeCount();
        this.roomNumber = command.roomNumber();
        this.accessPolicy = command.accessPolicy();
        this.minUsageMinutes = command.minUsageMinutes();
        this.maxUsageMinutes = command.maxUsageMinutes();
    }
}