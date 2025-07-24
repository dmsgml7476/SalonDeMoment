package com.salon.entity;

import com.salon.entity.shop.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    private int rating;

    @Lob // varchar -> text
    private String comment;

    // 작성일
    private LocalDateTime createAt;
    private String replyComment;
    private LocalDateTime replyAt;


}
