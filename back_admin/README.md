# Тестовый JSON для ProfileDto
### Данный JSON предоставляются фронтенду при запросе списка пользователей
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "user": {
    "full_name": "Иванов Иван Иванович",
    "email": "ivanov@example.com",
    "registration_date": "2023-05-15T10:30:00",
    "role": "EXPERT"
  },
  "birthday": "1985-03-20",
  "photo": "http://minio.example.com/user-avatars/550e8400-e29b-41d4-a716-446655440000.jpg",
  "passport": "http://minio.example.com/user-passports/550e8400-e29b-41d4-a716-446655440000.pdf",
  "diplom": "http://minio.example.com/user-diploms/550e8400-e29b-41d4-a716-446655440000.pdf",
  "phone": "+79991234567",
  "location": {
    "country": "Россия",
    "region": "Московская область",
    "city": "Москва",
    "address": "ул. Ленина, д. 10"
  },
  "profile_status": {
    "verification_result": "APPROVED",
    "activity_status": "ACTIVE"
  },
  "directions": [
    {
      "id": "dir1",
      "name": "Программирование"
    },
    {
      "id": "dir2",
      "name": "Анализ данных"
    }
  ],
  "instruments": [
    {
      "id": "inst1",
      "name": "Компьютер",
      "number": "INV-001",
      "date": "2024-01-10"
    },
    {
      "id": "inst2",
      "name": "Аналитическое ПО",
      "number": "INV-002",
      "date": "2023-12-15"
    }
  ],
  "additional_diplomas": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "number": "AD-001",
      "issue_date": "2020-06-15",
      "institution": "Московский университет",
      "specialty": "Информационные технологии",
      "degree": "Магистр",
      "link": "http://minio.example.com/user-additional-diploms/550e8400-e29b-41d4-a716-446655440000_a1b2c3d4-e5f6-7890-abcd-ef1234567890.pdf"
    }
  ],
  "certificates": [
    {
      "id": "b2c3d4e5-f678-9012-3456-7890abcdef12",
      "name": "Сертификат Java",
      "issue_date": "2021-09-10",
      "organization": "Oracle",
      "number": "CERT-001",
      "link": "http://minio.example.com/user-certs/550e8400-e29b-41d4-a716-446655440000_b2c3d4e5-f678-9012-3456-7890abcdef12.pdf"
    }
  ],
  "qualifications": [
    {
      "id": "c3d4e5f6-7890-1234-5678-90abcdef1234",
      "course_name": "Курс по машинному обучению",
      "issue_date": "2022-03-25",
      "institution": "Coursera",
      "number": "QUAL-001",
      "link": "http://minio.example.com/user-qualification/550e8400-e29b-41d4-a716-446655440000_c3d4e5f6-7890-1234-5678-90abcdef1234.pdf"
    }
  ]
}
```

---

# Тестовый JSON для ProfileCancelFromFront
### Данный JSON должен предоставляться фронтендом в случае отмены валидации профиля эксперта
```json
{
  "profile_id": "550e8400-e29b-41d4-a716-446655440000",
  "need_passport_delete": true,
  "need_diplom_delete": false,
  "directions": [
    "dir1",
    "dir2"
  ],
  "instruments": [
    "inst1"
  ],
  "additional_diploms": [
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "b2c3d4e5-f678-9012-3456-7890abcdef12"
  ],
  "certificates": [
    "c3d4e5f6-7890-1234-5678-90abcdef1234"
  ],
  "qualifications": [
    "d4e5f6g7-8901-2345-6789-0abcdef12345"
  ],
  "user_mail": "ivanov@example.com"
}
```