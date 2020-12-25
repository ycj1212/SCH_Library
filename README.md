# SCH_Library

### 소개

온라인 서점 프로그램 (데이터베이스 텀 프로젝트)

### 기능

- 관리자
  - 회원관리
  - 도서관리
  - 회원 별 구매내역
    - 주문내역 상세보기
  - 주문상황
    - 주문승인, 삭제
- 사용자
  - 회원정보(id, pw, name, card, address)
    - 수정, 회원탈퇴
  - 주문내역
    - 주문취소, 수취확인
  - 도서목록
    - 주문, 장바구니 담기
  - 장바구니
    - 주문, 삭제
  
### 개발환경

- Android Studio(Kotlin, XML)
- Virtual Box(Ubuntu server 18.04)
- LAPM(Linux Apache2 PHP MySQL)

### 배운점

- 서버를 직접 구축하여 데이터베이스와 연동하는 과정
- 웹 서버에 REST API 요청하여 JSON 데이터로 응답받는 과정
- RecyclerView, BottomNavigationView, ViewPager의 구성
- Activity와 Fragment간의 데이터 전달

### 결과

- 사용자의 편리성을 중심으로 개발하였으나, 뒤로갈수록 코드가 복잡해지고 중복된 코드들이 많아졌음  
- 그래서 디자인 패턴을 학습하여 적용해볼 예정
