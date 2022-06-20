# WebMail

### Web Mail service


## 기술 스택

1. java
2. spring boot
3. jpa
4. html
5. java script
6. mysql

## 개발 환경

- 서버 환경 - hmailserver (윈도우 메일서버), mysql
- 메인 환경 - intellij

## 프로젝트 목표

### 회원가입 - 로그인 - 메일 사용이 가능한 웹 페이지 개발

1. 메일 서버 구축
2. 웹 구축
3. 웹 프로젝트에서 서버에 원격 연결
4. 웹 메일 사용 테스트

## 프로젝트 내용

### 메일 서버 구축
윈도우 환경에서 사용할 수 있는 메일 서버인 hmailserver의 기본 세팅

1. 서버 컴퓨터의 SMTP, IMAP 포트 할당 및 연결
2. DB테이블 생성, 테스트용 사용자 계정 생성
3. 기본 보안 설정 (SSL인증서, SPF등록)
4. hmailserver의 각종 항목들과 일치하는 데이터 테이블 확인


### 웹 구축

메일서버, 데이터베이스를 연결하여 웹에서 사용할 모든 서비스 구현

DB와 연결되는 모든 부분은 Jpa Repository를 사용함

front-end 부분은 html과 간단한 java script를 사용하여 각종 기능을 테스트 할 수 있도록 구현함

#### 1. 로그인 서비스

- 회원가입

  회원 정보 기입 후 회원 가입을 진행하면 DB에 해당 정보 저장.
  
  회원가입 시 메일 서버에 저장되는 password encoder와 웹에서 사용하는 password encoder의 형식이 다르기 때문에
  DB에 저장할 때 2가지의 encoder를 사용하여 각각 저장하였음
  
  !중복 아이디와 비밀번호 재입력을 확인하여 예외처리
  
  
- 로그인/로그아웃
  
  로그인 성공 시 로그인 성공 페이지 이동
  
  로그인 실패 시 경고창과 함께 원래 페이지로 이동
  
  !로그인 success / fail handler를 이용하여 예외 처리


- 회원 목록 확인

  특정 직급 이상만 확인할 수 있는 페이지 (접근 권한 부여)
  
  회원가입 된 모든 회원의 정보를 열람 가능
  
  정보 상세보기 페이지를 따로 두어 회원 목록에는 간단한 정보, 상세보기 페이지에는 해당 회원의 모든 정보 접근 가능
  
  !접근 권한이 없는 계정의 경우 접근 불가 
  
  
- 회원 정보 수정
  
  회원의 모든 정보를 수정할 수 있도록 함


#### 2. 메일 서비스

- 메일 서버의 메일함 생성 및 접속 (보낸 메일, 받은 메일, 임시 메일, 스팸 메일, 휴지통)

  필요한 메일함을 생성하고, 메일 서버의 ip, port, foldername을 통해 각 메일폴더에 로그인
  
  
- 메일 보내기 (참조, 숨은 참조, 첨부 파일, 임시 저장)

  properties와 session에 메일 서버 및 로그인 된 사용자의 정보를 입력하여 메일 서버에 로그인
  
  MimeMessage에 보내는 사람, 받는사람, 참조, 숨은 참조, 제목, 내용, 첨부파일의 정보를 입력하여 전송 (첨부파일과 내용은 multmipart로 저장)
  
  보낸 메일은 보낸 메일 폴더에 저장, 임시 저장을 선택한 경우 임시 메일 폴더에 저장
  
  첨부파일을 보낼 경우 해당 파일을 multpartFile 형태로 request하여 서버에 저장한 후 해당 경로의 파일을 보내도록 
  
  !이메일 형식이 아닐 경우 경고표시
  
  
- 메일 상세보기

  컨트롤러로 부터 선택한 메일의 uid를 가져와 해당 메일의 상세 정보를 나타내는 기능
  
  메일 내용은 multipart로 불러와 형식에 맞게 출력, 첨부파일 확인
  
  
- 메일 삭제/영구삭제

  휴지통을 제외한 각 메일함에서 선택된 모든 메일을 삭제하는 기능
  
  휴지통 이외의 폴더에서 삭제한 메일은 휴지통 폴더로 이동
  
  휴지통에서 선택된 메일은 영구 삭제함
  
  !선택된 항목이 없을 경우 경고
  
  
- 메일 답장

  선택된 메일에 대한 답장을 하는 기능
  
  메일 헤더에 존재하는 reply-to를 불러와 받는 사람 항목에 나타냄
  
  선택된 메일의 원본을 메일 내용 하단에 출력
  
  !선택된 항목이 없을 경우 경고
  

- 메일 전달

  선택된 메일의 내용을 전달하는 기능
  
  선택된 메일의 원본 내용을 메일 내용에 출력
  
  !선택된 항목이 없을 경우 경고
  
  
- 메일 이동

  선택된 메일을 지정한 폴더로 이동하는 가능
  
  선택 된 메일을 복사하여 지정한 폴더에 복사 후 원본 삭제
  
  !보낸 메일은 받은 메일함으로 이동 불가하며 그 반대의 경우도 불가함. 이 경우 예외처리 및 경고 표시
  
  !선택된 메일이 없을 경우 경고
  
  
- 스팸 주소 설정/해제

  스팸 주소를 등록 및 해제하는 기능
  
  !등록되는 스팸 주소가 이메일 형식이 아닐 경우 예외처리 및 경고 표시
  
  !이미 등록 된 주소일 경우 예외처리 및 경고 표시
  
  
- 메일 읽음 표시

  메일을 확인하였는지 표시하는 기능
  
  메일 목록에서 읽음/안읽음 으로 표시되며 읽음 버튼을 누를 경우 해당 메일이 읽음으로 표시됨
  
  메일의 /seen flag를 통해 해당 상태 판별
  
  
- 중요 메일 설정/해제

  해당 메일이 중요한 메일인지 표시하는 기능
  
  메일 목록에서 설정/해제로 표시되며 해당 버튼을 누를 경우 상태가 바뀜
  
  메일의 /important flag를 통해 해당 상태 판별
  
  
- 전체 메일함, 중요 메일함, 안읽은 메일함 구현

  따로 폴더를 생성하지 않고 구현이 가능한 부분이기에 메일서버에 존재하는 폴더는 아님
  
  전체 메일함의 경우 모든 메일 폴더에 존재하는(휴지통, 스팸 메일 제외) 메일을 가져옴
  
  중요 메일함의 경우 모든 메일 중 /important flag가 존재하는 메일만을 검색하여 가져옴
  
  안읽은 메일함의 경우 모든 메일 중 /seen flag가 존재하는 메일만을 검색하여 가져옴


### 메일 및 DB 원격 연결
서버 컴퓨터의 메일 서버 및 DB 원격 접속이 가능하도록 함
DB의 경우 Intellij database 연결 기능을 이용하여 ip, id, password를 통해 접속
메일 서버는 java mail api를 이용하여 ip, id, password를 통해 접속

### 웹 메일 사용 테스트
구현된 기능 정상 작동 테스트 결과 모든 기능이 정상적으로 작동함
