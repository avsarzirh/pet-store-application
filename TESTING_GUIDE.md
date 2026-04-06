# PetStore Uygulaması - API Test Rehberi

Bu belge, PetStore Uygulamasındaki tüm uç noktaları (endpoints) test etmek için örnek JSON verilerini ve talimatları içerir.

## 🚀 Nasıl Test Edilir?

API'yi test etmek için aşağıdaki araçları kullanabilirsiniz:
- **Postman** (Önerilen)
- **Insomnia**
- **VS Code REST Client** (projedeki `test.http` dosyasını kullanarak)
- **cURL**

---

## 🔐 1. Kimlik Doğrulama (Auth)

İşlem yapabilmek için önce kayıt olmalı ve JWT Token almak için giriş yapmalısınız.

### Kullanıcı Kaydı (Register)
`POST /auth/register`

**Örnek İstek (JSON):**
```json
{
  "username": "petowner_alpha",
  "email": "alpha@example.com",
  "password": "strongpassword123",
  "isStoreOwner": true
}
```

### Kullanıcı Girişi (Login)
`POST /auth/login`

**Örnek İstek (JSON):**
```json
{
  "login": "petowner_alpha",
  "password": "strongpassword123"
}
```
> **Not:** Yanıttaki `token` değerini kopyalayın. Diğer isteklerde `Authorization` başlığına şu şekilde eklemeniz gerekecek: `Bearer SİZİN_TOKEN_DEĞERİNİZ`.

---

## 🏪 2. Mağaza Yönetimi (Store)

Sadece `STORE_OWNER` (Mağaza Sahibi) rolüne sahip kullanıcılar mağaza oluşturabilir, güncelleyebilir veya silebilir.

### Mağaza Oluştur
`POST /store`
*Yetkilendirme Gerekir*

**Örnek İstek (JSON):**
```json
{
  "name": "Alfa Evcil Hayvan Mağazası"
}
```

### Mağaza Güncelle
`PUT /store/{id}`
*Yetkilendirme Gerekir*

**Örnek İstek (JSON):**
```json
{
  "name": "Alfa Evcil Hayvan Mağazası - Güncellendi"
}
```

### Tüm Mağazaları Listele
`GET /store`

### ID ile Mağaza Getir
`GET /store/{id}`

### Mağaza Sil
`DELETE /store/{id}`
*Yetkilendirme Gerekir*

---

## 🐾 3. Evcil Hayvan Yönetimi (Pet)

Evcil hayvanlar belirli bir mağazaya bağlıdır.

### Evcil Hayvan Oluştur
`POST /pet`
*Yetkilendirme Gerekir*

**Örnek İstek (JSON):**
```json
{
  "name": "Pamuk",
  "age": 2,
  "price": 450.0,
  "type": "DOG",
  "gender": "MALE",
  "storeId": 1
}
```

### Evcil Hayvan Güncelle
`PUT /pet/{id}`
*Yetkilendirme Gerekir*

**Örnek İstek (JSON):**
```json
{
  "name": "Pamuk (Goldie)",
  "age": 3,
  "price": 500.0,
  "type": "DOG",
  "gender": "MALE",
  "storeId": 1
}
```

### Tüm Hayvanları Listele (Filtreli)
`GET /pet?type=DOG&gender=MALE&page=0&size=10`

### ID ile Hayvan Getir
`GET /pet/{id}`

### Mağaza ID'sine Göre Hayvanları Getir
`GET /pet/store/{storeId}`

### Evcil Hayvan Sil
`DELETE /pet/{id}`
*Yetkilendirme Gerekir*

---

## 💡 Önemli Veri Tipleri

### Hayvan Türleri (Animal Types)
- `CAT` (Kedi)
- `DOG` (Köpek)
- `HAMSTER`
- `BIRD` (Kuş)
- `FISH` (Balık)
- `TURTLE` (Kaplumbağa)
- `EXOTIC` (Egzotik)
- `OTHER` (Diğer)

### Cinsiyetler (Genders)
- `MALE` (Erkek)
- `FEMALE` (Dişi)
