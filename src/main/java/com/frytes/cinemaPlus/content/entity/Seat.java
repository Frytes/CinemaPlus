package com.frytes.cinemaPlus.content.entity;

import com.frytes.cinemaPlus.content.entity.enumps.SeatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hall_id", "row_index", "col_index"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    public Hall hall;

    @Column(nullable = false)
    public Integer rowIndex; // Y

    @Column(nullable = false)
    public Integer colIndex; // X

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatType type;

    @Column(name = "seat_number", nullable = false)
    public String seatNumber;
}
