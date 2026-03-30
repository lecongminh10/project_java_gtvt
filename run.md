Chạy: mvn spring-boot:run

setup 
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:orcl
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.username=mimi
spring.datasource.password=Mimi123
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true