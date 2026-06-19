package com.example.boardapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.boardapp.domain.Member;
import com.example.boardapp.domain.Post;
import com.example.boardapp.dto.PostDetailsResponse;
import com.example.boardapp.dto.PostListResponse;
import com.example.boardapp.dto.PostWriteRequest;
import com.example.boardapp.mapper.MemberMapper;
import com.example.boardapp.mapper.PostMapper;

import jakarta.validation.Valid;

@Controller // 컨트롤러 빈으로 등록합니다
@RequestMapping("/posts") // URL prefix: 이 컨트롤러가 처리하는 URL 앞에 공통적으로 붙입니다
public class PostController {
  
  // 의존성 선언하기
  private final PostMapper postMapper; // 게시물 데이터 처리
  private final MemberMapper memberMapper; // 멤버 데이터 처리

  public PostController(PostMapper postMapper, MemberMapper memberMaper) {
    this.postMapper = postMapper;
    this.memberMapper = memberMaper;
  }


  // 글 작성페이지 요청 핸들러
  @GetMapping("/write") // GET /write 요청을 처리합니다
  public String writeForm(Model model) {
    
    // 뷰의 postForm(게시물 작성폼)과 게시물 생성 DTO(PostWriteRequest)를 매핑합니다.
    model.addAttribute("postForm", new PostWriteRequest());
    
    // 서버의 응답
    return "post/write"; // 게시물 작성페이지를 전송합니다.
  }

  // 글 작성을 처리하는 핸들러
  @PostMapping // POST /prefix(/posts)
  // dto 앞에 @Valid, @ModelAttribute 어노테이션이 붙은 상태
  // @Valid: 사전에 정의한 유효성 검사를 실행하라
  // @ModelAttribute: postForm(게시물 작성폼)과 dto를 매핑합니다.
  // BindingResult: validation이 유효성 검사를 수행하고 결과(에러)를 담은 객체
  // Principal: 인증이 완료된 사용자 정보(로그인 중인 사용자)를 저장하고 있는 객체
  public String write(@Valid @ModelAttribute("postForm") PostWriteRequest dto, BindingResult bindingResult, Principal principal) {
    
    // 게시물 유효성 검사를 통과하지 못한 경우. 예) 사용자가 글 제목이나 본문을 입력안하고 전송한 경우
    if (bindingResult.hasErrors()) {
      // 게시물 작성페이지를 전송합니다. 오류메시지와 함께
      return "post/write";
    }

    // 로그인한 멤버를 확인합니다 (데이터베이스 저장용)
    // principal.getName(): 로그인 중인 사용자의 유저네임을 반환합니다
    Member loginMember = memberMapper.findByUsername(principal.getName())
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

    // 게시물을 작성자 정보와 함께 저장합니다
    Post post = new Post(dto.getTitle(), dto.getContent(), loginMember);
    postMapper.save(post);

    // 방금 작성한 글 보기 페이지로 이동합니다.
    return "redirect:/posts/details?id=" + post.getId();
    // .xml의 useGeneratedKey=true를 활용하는 부분이 여기입니다.
    // 게시물 생성 후 바로 id에 접근 가능
  }

  // 게시물 목록 페이지 요청 핸들러
  @GetMapping // GET /prefix 요청을 처리합니다
  public String list(Model model) {

    // 데이터베이스로부터 가져온 게시물들로부터 글 목록DTO(PostListResponse)를 생성하는 과정
    List<PostListResponse> posts = postMapper.findAll().stream()
      .map((post) -> {
        PostListResponse dto = new PostListResponse();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        // 멤버 정보중 이름만 가져옵니다
        dto.setMemberName(post.getMember().getUsername());
        return dto;
      })
      .collect(Collectors.toList());

    // 뷰에게 dto를 전달합니다.
    // posts(attributeName): 뷰에서 이 dto를 저장하는 부분
    // 뷰는 이 데이터를 바탕으로 화면을 완성합니다.
    model.addAttribute("posts", posts);

    // 서버의 응답 (글 목록페이지를 전송합니다)
    return "post/list";
  }

  // 게시물 상세보기 페이지
  @GetMapping("/details")
  // id 매개변수 앞에 @RequestParam 어노테이션이 붙은 상태
  // @RequestParam: 요청-매개변수를 의미합니다.
  // 이 요청 매개변수에 사용자가 전송한 게시물의 id가 저장되어 있습니다. 예) /details?id=99
  // 주소?요청 매개변수 키=값
  public String details(@RequestParam("id") Long id, Model model) {
    
    // id에 해당하는 게시물을 검색합니다.
    Post post = postMapper.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다"));

    // 게시물로부터 글 상세보기 DTO(PostDetailsResponse)를 생성합니다.
    PostDetailsResponse dto = new PostDetailsResponse();
    dto.setId(post.getId());
    dto.setTitle(post.getTitle());
    dto.setContent(post.getContent());
    dto.setMemberName(post.getMember().getUsername());

    // 모델이 뷰에게 dto를 전달합니다. 뷰는 화면을 이제 완성할 수 있습니다.
    // 뷰의 'post' 자리에 이 dto가 들어옵니다
    model.addAttribute("post", dto);

    // 서버의 응답 (글 상세페이지 전송)
    return "post/details";
  }

  // 게시물 삭제 요청 핸들러
  @PostMapping("/delete")
  // id: 사용자가 삭제를 요청한 게시물의 id
  // Principal: 현재 로그인 중인 사용자 정보
  public String remove(@RequestParam("id") Long id, Principal principal) {

    // 삭제할 게시물을 검색합니다
    Post post = postMapper.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글: " + id));
    // 예외를 던지면 예외처리기에서 400에러(BadRequest)로 처리를 할겁니다
    
    // 로그인한 멤버를 확인합니다.
    Member loginMember = memberMapper.findByUsername(principal.getName())
      .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다"));

    // 삭제를 요청한 사용자가 이 게시물의 작성자인지 확인합니다.
    if (post.getMember().getId().equals(loginMember.getId())) {
      // 게시물을 삭제합니다
      postMapper.delete(id);
    }

    // 게시물 목록페이지로 돌아갑니다
    return "redirect:/posts";
  }
}
