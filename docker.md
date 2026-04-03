# Huong dan chay Docker

## Yeu cau
- Docker va Docker Compose

## Chay ung dung
1. Vao thu muc docker:
   ```bash
   cd docker
   ```
2. Build va chay toan bo service:
   ```bash
   docker compose up --build -d
   ```

## Kiem tra
- App: http://localhost:8081
- Adminer: http://localhost:8082

## Thong tin MySQL
- Host trong Docker network: mysql
- Port ngoai may: 3306
- Database: project_gtvt
- Username: root
- Password: 12345678

## Xem log
- Tat ca:
  ```bash
  docker compose logs -f
  ```
- Rieng app:
  ```bash
  docker compose logs -f app
  ```

## Dung va xoa container
- Dung:
  ```bash
  docker compose down
  ```
- Dung va xoa volume (mat du lieu DB):
  ```bash
  docker compose down -v
  ```
