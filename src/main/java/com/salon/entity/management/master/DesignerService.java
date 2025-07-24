package com.salon.entity.management.master;

import com.salon.constant.ServiceCategory;
import com.salon.entity.Member;
import com.salon.entity.management.ShopDesigner;
import com.salon.entity.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Entity
public class DesignerService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designer_service_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_designer_id")
    private ShopDesigner shopDesigner;

    private boolean cut;
    private boolean color;
    private boolean perm;
    private boolean upstyle;
    private boolean dry;
    private boolean hair_extension;
    private boolean clinic;

    public void setAssignedCategories(List<ServiceCategory> categories) {
        // 초기화 후
        this.cut = false;
        this.color = false;
        this.perm = false;
        this.upstyle = false;
        this.dry = false;
        this.hair_extension = false;
        this.clinic = false;

        // 리스트에 포함된 카테고리는 true 로 변환
        for (ServiceCategory category : categories) {
            switch(category) {
                case CUT: this.cut = true; break;
                case COLOR: this.color = true; break;
                case PERM: this.perm = true; break;
                case UPSTYLE: this.upstyle = true; break;
                case DRY: this.dry = true; break;
                case HAIR_EXTENSION: this.hair_extension = true; break;
                case CLINIC: this.clinic = true; break;
            }
        }
    }

    // 담당 카테고리 리스트로 변환
    public List<ServiceCategory> getAssignedCategories() {
        List<ServiceCategory> categories = new ArrayList<>();

        if (this.cut) categories.add(ServiceCategory.CUT);
        if (this.color) categories.add(ServiceCategory.COLOR);
        if (this.perm) categories.add(ServiceCategory.PERM);
        if (this.upstyle) categories.add(ServiceCategory.UPSTYLE);
        if (this.dry) categories.add(ServiceCategory.DRY);
        if (this.hair_extension) categories.add(ServiceCategory.HAIR_EXTENSION);
        if (this.clinic) categories.add(ServiceCategory.CLINIC);

        return categories;
    }



}
