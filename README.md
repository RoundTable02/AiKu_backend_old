<h1 align="center" style="font-weight: bold;">지각을 방지하는 약속 관리 어플리케이션 AIKU ✨</h1>
<p align="center">
  <img align='center' src='https://github.com/user-attachments/assets/6c5c8c08-c355-4ccb-a0b7-19613ba97fd6' width="700"/></img>
</p>
<p align="center">
  약속을 지키는 그날까지 <b>"약속을 편리하게 관리하고, 재미있게 지각을 방지하자”</b>
</p>
<p align="center">기간 | 2024.04 ~ 2024.06.10</p>
<p align="center">팀원 | 곽유나, 최원탁</p>

<div align="center">
  
  *세부 기능 및 기획은 아래 링크를 참고해주세요.* </br> https://github.com/kyoona/AiKu_backend
  
</div>

<div align="center">
  
  ### ER Diagram
  
  <img width=800 src="https://github.com/user-attachments/assets/c3abe611-f7d2-4df4-a3cb-e2474c71b4b1" />


</div>
<br/>

<h2 id="technologies">🛠️ 기술</h2>

| Category | Stack |
| --- | --- |
| Language | Java |
| Framework | Spring Boot |
| Library | Spring Data JPA, Query DSL |
| Database | H2 |
| Infra | AWS |
| Cloud Service | Firebase Messaging |

</br>

<h2>👩🏻‍💻구현 파트</h2>

### 최원탁

기획과 ERD 작성, 백엔드 서버 기능 개발을 담당하였습니다.   

<b>사용자 인증 기능</b><br/>
- Spring Security를 통한 JWT 인증 인가 로직을 작성하였습니다.
- 카카오 로그인 및 회원가입을 구현하고, 테스트 코드를 작성하였습니다.


<b>아이템 및 칭호 부여</b><br/>
- 아이템 구매 및 사용 로직을 MVC 패턴을 이용하여 구현하였습니다.
- 스프링 @EventListener를 이용하여 스케줄 종료 이벤트를 감지하고, 칭호를 자동 부여하는 기능을 작성하였습니다.

<b>베팅 시스템</b><br/>
- 베팅을 추가하고, 변경하는 로직을 작성하였습니다.
- 스프링 @EventListener를 이용하여 스케줄 종료 이벤트를 감지하고, 베팅 포인트 정산을 진행하는 기능을 작성하였습니다.
- 그룹 베팅 내역에 대한 정산 로직을 작성하고, 이 결과를 텍스트로 저장하여 불러오는 시간을 단축하였습니다.
  
<b>AI 서버 연동</b><br/>
- 음성 인식을 통해 요일, 시간, 장소를 텍스트로 추출하는 AI API 서버에 대해 HTTP로 음성 파일을 전달하고, 결과값을 반환하는 로직을 개발하였습니다.

<b>ETC</b><br/>
- @AuthenticationPrincipal으로 사용자 정보를 가져오던 중 해당 방식으로 가져온 사용자에 대해 준영속 상태로 가져오는 것을 확인하였습니다. JPA의 편리한 Update를 이용하기 위해 OSIV를 필터 단까지 확장시켜 문제를 해결하였습니다.
- AWS S3를 이용하여 사진 업로드 로직을 추가하였습니다.
- 스프링 @EventListener를 이용하여 사용자 포인트 증감에 대한 관심사를 분리하고, 한 서비스에서 증감과 내역 로그를 기록할 수 있도록 구성하였습니다.
- data transfer object를 conroller계층과 service계층, repository 계층으로 분리하였습니다.   
  DTO의 계층 구조는 controller dto, service dto, entity로 되어 있으며 controller dto와 service dto의 분리로 서비스 계층은 view에 대한 의존을 최소화 합니다.   
  view에 변화가 생겨 제공해야 할 변수에 변경이 있어도 service계층은 영향을 받지 않습니다. 디자인과 구현이 동시에 진행되어 뷰의 구조가 계속해서 바꼈던 저희 프로젝트를 위해 이와 같은 구조를 채택하였습니다.
- ExceptionHandler를 통해 에러 관리 로직을 서비스 로직으로부터 분리하고, 클라이언트에게 일관된 응답을 할 수 있도록 합니다.

