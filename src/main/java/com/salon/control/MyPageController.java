package com.salon.control;

import com.salon.config.CustomUserDetails;
import com.salon.constant.Role;
import com.salon.dto.admin.CsListDto;
import com.salon.dto.shop.CouponListDto;
import com.salon.dto.user.*;
import com.salon.entity.Member;
import com.salon.service.admin.CsService;
import com.salon.service.user.MyReservationService;
import com.salon.service.user.MypageService;
import com.salon.service.user.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/myPage")
public class MyPageController {

    private final MyReservationService myReservationService;
    private final ReviewService reviewService;
    private final MypageService mypageService;
    private final CsService csService;


    @GetMapping("/reservation")
    public String myReservationPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long memberId = userDetails.getMember().getId();

        List<MyReservationDto> reservationDtoList = myReservationService.getMyReservations(memberId);

        model.addAttribute("myReservations", reservationDtoList);

        return "/user/myReservation";
    }


    @PostMapping("/review/create")
    public String reviewSave(@ModelAttribute ReviewCreateDto dto) {
        reviewService.saveReview(dto);
        return "redirect:/myPage/reservation";
    }

    // 내쿠폰/정액권


    @GetMapping("/coupons")
    public String myCouponsPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();

        List<CouponListDto> couponListDtos = mypageService.getMyCoupons(memberId);
        List<MyTicketListDto> myTicketListDtos = mypageService.getMyTicket(memberId);

        model.addAttribute("myCoupons", couponListDtos);
        model.addAttribute("myTickets", myTicketListDtos);
        System.out.println("정액권 개수: " + myTicketListDtos.size());

        return "/user/myCoupons";
    }



    //찜목록

    @GetMapping("/likeList")
    public String likeList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long memberId=userDetails.getId();

        List<LikeDesignerDto> likeDesignerDtos = mypageService.getDesignerLike(memberId);

        List<LikeShopDto> likeShopDtos = mypageService.getShopLike(memberId);

        model.addAttribute("myLikeDesigner", likeDesignerDtos);
        model.addAttribute("myLikeShop", likeShopDtos);

        return "/user/myLike";
    }


    //리뷰

    @GetMapping("/review")
    public String myReviewPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long memberId = userDetails.getId();

        // 초기 데이터만 0페이지 size=9로 페이징 조회
        Pageable pageable = PageRequest.of(0, 9, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<MyReviewListDto> page = mypageService.getMyReviewList(memberId, pageable);

        model.addAttribute("reviewList", page.getContent());
        model.addAttribute("hasNext", page.hasNext()); // JS에서 필요하면 사용 가능
        return "/user/myReview";
    }

    // 무한 스크롤 요청 처리용
    @GetMapping("/review/page")
    @ResponseBody
    public ResponseEntity<Page<MyReviewListDto>> getReviewPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

        Page<MyReviewListDto> reviewPage = mypageService.getMyReviewList(memberId, pageable);
        return ResponseEntity.ok(reviewPage);
    }

//    @GetMapping("/review")
//    public String myReviewPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
//        Long memberId = userDetails.getId();
//
//        List<MyReviewListDto> myReviewListDtos = mypageService.getMyReviewList(memberId);
//
//        model.addAttribute("reviewList", myReviewListDtos);
//
//        return "/user/myReview";
//    }

    @GetMapping("/review/{reviewId}")
    public ResponseEntity<MyReviewDetailDto> myReviewDetail(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        MyReviewDetailDto dto = mypageService.getMyReviewDetail(reviewId, memberId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/myQuestionList")
    public String myQuestionList(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<CsListDto> csListDtoList = csService.List();
        Member member = userDetails.getMember();
        if(member.getRole() == Role.ADMIN){
            csListDtoList = csService.findAll();
        } else {
            csListDtoList = csService.findByMember(member);
        }
        model.addAttribute("csListDtoList", csListDtoList);
        return "user/myQuestionList";
    }

}
