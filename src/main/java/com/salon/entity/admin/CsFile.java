package com.salon.entity.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="cs_file")
public class CsFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="cs_customer_id")
    private CsCustomer csCustomer;

    private String originalName;
    private String fileName;
    private String fileUrl;

}
