# DB Schema (Draft)

이 문서는 현재 JPA 엔티티 기준의 DB 스키마 초안입니다.
실제 운영 스키마와 차이가 있을 수 있으므로, 변경 시 함께 업데이트하세요.

## 1. 테이블 목록

- `users`
- `social_account`
- `school`
- `classroom`
- `class_code`
- `student`
- `parent_student`
- `chat_room`
- `message`

모든 테이블은 공통 컬럼을 가집니다.
- `created_at` (NOT NULL)
- `updated_at` (NOT NULL)

## 2. 테이블 요약

### `users`
- PK: `id`
- 컬럼: `name`, `login_id`, `pw`, `role`

### `social_account`
- PK: `id`
- FK: `user_id -> users.id` (NOT NULL)
- 컬럼: `provider` (NOT NULL), `provider_user_id` (NOT NULL)

### `school`
- PK: `id`
- 컬럼: `name`

### `classroom`
- PK: `id`
- FK: `school_id -> school.id`
- FK: `teacher_id -> users.id`
- 컬럼: `grade`, `class_number`

### `class_code`
- PK: `id`
- FK: `classroom_id -> classroom.id` (NOT NULL)
- 컬럼: `code` (NOT NULL)

### `student`
- PK: `id`
- FK: `classroom_id -> classroom.id`
- 컬럼: `name`, `birthday`, `gender`

### `parent_student`
- PK: `id`
- FK: `parent_id -> users.id` (NOT NULL)
- FK: `student_id -> student.id` (NOT NULL)

### `chat_room`
- PK: `id`
- 컬럼: `intent_label`, `status`

### `message`
- PK: `id`
- FK: `chat_room_id -> chat_room.id`
- FK: `sender_id -> users.id`
- 컬럼:
  - `type`
  - `content` (TEXT)
  - `modify_content` (TEXT)
  - `similarity_original` (FLOAT)
  - `similarity_modified` (FLOAT)
  - `is_dispute_risk` (BOOLEAN)

## 3. 관계 요약

- `users (1) - (N) social_account`
- `users (1) - (N) classroom` (`teacher_id`)
- `school (1) - (N) classroom`
- `classroom (1) - (N) class_code`
- `classroom (1) - (N) student`
- `users (1) - (N) parent_student` (`parent_id`)
- `student (1) - (N) parent_student`
- `chat_room (1) - (N) message`
- `users (1) - (N) message` (`sender_id`)

## 4. 참고

- 현재 애플리케이션 설정은 `spring.jpa.hibernate.ddl-auto=validate` 입니다.
- 즉, 애플리케이션이 테이블을 생성하지 않고, 사전 준비된 스키마를 검증만 합니다.
