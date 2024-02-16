****************************************************************************************************************
ㄱ.mac mysql 완전삭제... -> 매우 헤맸음 그래서 정리해둠,,,,
****************************************************************************************************************
1. 설치를 dmg 통해 mysql 설치를 진행하였음.
2. 근데 너무 오래되서 어떻게 설치를 하였는지 까먹었었음...
3. 그래서 계속 rm -rf /usr/local/../mysql*  <-이런식으로(에시임) mysql 폴더 아래에 있는 mysql 폴더를 찾아서 삭제하였음..
4. 삭제 완료된줄 알고 homebrew 통해 mariadb / mysql 등 계속 설치했다 삭제 했다 반복함...;
5. 계속 mariadb 제대로 설치가 안되고 mysql이 남아있었음..
6. 결론 삭제 방법은 아래와 같음!!!!!!

<homebrew -> brew install mysql>로 설치했을때, 
1. sudo rm -rf /usr/local/var/mysql
2. sudo rm -rf /usr/local/bin/mysql*
3. sudo rm -rf /usr/local/Cellar/mysql
 
<MySQL 공식홈페이지의 DMG파일>로 설치했을때,
1. sudo rm -rf /usr/local/mysql
2. sudo rm -rf /Library/PreferencePanes/My*
3. sudo rm -rf /var/db/receipts/com.mysql.*
****************************************************************************************************************
