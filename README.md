# ModernBank Authentication Service

ModernBank Authentication Service, ModernBank mikroservis mimarisi içinde kullanıcı kimlik doğrulama ve yetkilendirme işlemlerini yöneten Spring Boot tabanlı bir servistir. Kullanıcı kayıt işlemlerini hesap servisi üzerinden işler, PBKDF2 ile şifreler ve AES ile şifrelenmiş JWT token üretir. Redis destekli kara liste mekanizması ile token iptallerini yönetir, hata yönetimi için Parameter Service ile entegre çalışır.

## İçindekiler
- [Özellikler](#özellikler)
- [Mimari ve Teknolojiler](#mimari-ve-teknolojiler)
- [Proje Yapısı](#proje-yapısı)
- [Ön Koşullar](#ön-koşullar)
- [Yapılandırma](#yapılandırma)
  - [Uygulama özellikleri](#uygulama-özellikleri)
  - [Örnek `application.yml`](#örnek-applicationyml)
  - [Dış servisler](#dış-servisler)
- [Projeyi Çalıştırma](#projeyi-çalıştırma)
- [Testler](#testler)
- [API Referansı](#api-referansı)
- [Güvenlik ve Token Yaşam Döngüsü](#güvenlik-ve-token-yaşam-döngüsü)
- [Hata Yönetimi ve Gözlemleme](#hata-yönetimi-ve-gözlemleme)
- [Zamanlanmış Görevler](#zamanlanmış-görevler)
- [Faydalı Maven Komutları](#faydalı-maven-komutları)

## Özellikler
- **JWT tabanlı oturum açma:** Kullanıcı doğrulaması sonrası AES ile şifrelenmiş JWT üretir ve rol bilgisini yanıtlar. `JwtService` sınıfı anahtar üretimi ve şifreleme işlemlerini yürütür.【F:src/main/java/com/modernbank/authentication_service/jwt/JwtService.java†L23-L115】
- **Token kara listeleme:** `BlackListService` token sonlanma zamanını Redis üzerinde TTL olarak saklayarak çıkış yapan kullanıcıların tokenlarını geçersiz kılar.【F:src/main/java/com/modernbank/authentication_service/service/impl/BlackListServiceImpl.java†L16-L35】
- **Kullanıcı kaydı delegasyonu:** Yeni kullanıcı oluşturma talepleri Feign Client aracılığıyla `account-service` servisine aktarılır.【F:src/main/java/com/modernbank/authentication_service/api/client/AccountServiceClient.java†L9-L13】【F:src/main/java/com/modernbank/authentication_service/service/impl/AuthenticationServiceImpl.java†L48-L52】
- **ModelMapper ile DTO <-> entity dönüşümleri:** `MapperService` ve `ApplicationConfiguration` sınıfları güçlü tip eşleştirme sağlar.【F:src/main/java/com/modernbank/authentication_service/service/impl/MapperServiceImpl.java†L14-L33】【F:src/main/java/com/modernbank/authentication_service/config/ApplicationConfiguration.java†L28-L37】
- **PBKDF2 tabanlı şifreleme:** `ApplicationConfiguration` PBKDF2WithHmacSHA256 algoritmasıyla kullanıcı parolalarını güvenli şekilde saklar.【F:src/main/java/com/modernbank/authentication_service/config/ApplicationConfiguration.java†L39-L51】
- **Kapsamlı hata yönetimi:** `GlobalExceptionHandler`, hata kodlarını Parameter Service üzerinden alır ve loglar, özel hata gövdeleri döner.【F:src/main/java/com/modernbank/authentication_service/exceptions/GlobalExceptionHandler.java†L28-L87】
- **JWT filtre zinciri:** `JwtAuthenticationFilter`, bearer token’ı çözerek kullanıcı doğrulaması yapar ve kara listelenmiş tokenları engeller.【F:src/main/java/com/modernbank/authentication_service/jwt/JwtAuthenticationFilter.java†L31-L95】
- **Planlı önbellek yenileme:** Hata kodları her saat Parameter Service’den çekilerek Redis önbelleği güncellenir.【F:src/main/java/com/modernbank/authentication_service/service/impl/ErrorCodeServiceImpl.java†L43-L63】

## Mimari ve Teknolojiler
- **Dil & Çatı:** Java 17, Spring Boot 3.2
- **Kimlik Doğrulama:** Spring Security, JWT (JJWT), AES şifreleme
- **Veri Saklama:** Spring Data JPA (varsayılan olarak MySQL), Redis (Lettuce) - hata kodu önbelleği ve token kara listesi
- **İletişim:** Spring Cloud OpenFeign ile diğer mikro servis çağrıları
- **Hata Yönetimi:** Parameter Service ile entegre hata kodu yönetimi ve loglama
- **İzleme:** Saatlik hata kodu önbellek yenileme scheduleri (Spring Scheduling)

## Proje Yapısı
```
modernbank-authentication-service/
├── pom.xml
├── src/
│   ├── main/java/com/modernbank/authentication_service/
│   │   ├── AuthenticationServiceApplication.java
│   │   ├── api/            # Request/response modelleri, Feign client'lar
│   │   ├── config/         # Güvenlik, Redis ve uygulama konfigürasyonları
│   │   ├── controller/     # REST endpoint'leri
│   │   ├── entity/         # JPA varlıkları (User, Account, ErrorCodes)
│   │   ├── exceptions/     # Global exception handler ve özel exception'lar
│   │   ├── jwt/            # JWT servisi, filtre ve AES yardımcıları
│   │   ├── model/          # Domain modelleri (DTO'lar)
│   │   ├── repository/     # Spring Data repository arayüzleri
│   │   └── service/        # İş mantığı arayüzleri ve implementasyonları
│   └── test/java/...       # Spring Boot context testleri
```

## Ön Koşullar
- Java 17 JDK
- Maven 3.9+
- Çalışan MySQL 8+ örneği (kullanıcı & şifre ile)
- Çalışan Redis örneği (varsayılan 6379) – token kara listeleme ve hata kodu önbelleği için
- Parameter Service ve Account Service uç noktaları (Feign client konfigürasyonları ile eşleşmeli)

### Docker ile hızlı servis başlatma (opsiyonel)
```bash
docker run -d --name modernbank-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=modernbank -p 3306:3306 mysql:8.0
docker run -d --name modernbank-redis -p 6379:6379 redis:7
```

## Yapılandırma
Servis, Spring Boot `application.yml`/`application.properties` veya ortam değişkenleri üzerinden yapılandırılır.

### Uygulama özellikleri
| Özellik | Açıklama |
| --- | --- |
| `spring.datasource.url` | MySQL bağlantı URL’i (`jdbc:mysql://localhost:3306/modernbank`) |
| `spring.datasource.username` / `spring.datasource.password` | Veritabanı kimlik bilgileri |
| `spring.jpa.hibernate.ddl-auto` | Şema yönetimi (`validate`, `update`, vb.) |
| `security.encryption.secret-key` | PBKDF2 için gizli anahtar (AES ile uyumlu uzunlukta)【F:src/main/java/com/modernbank/authentication_service/config/ApplicationConfiguration.java†L39-L50】 |
| `app.jwtSecret` | JWT imzalama anahtarı (Base64 kodlu)【F:src/main/java/com/modernbank/authentication_service/jwt/JwtService.java†L31-L60】 |
| `app.jwtCryptoSecret` | JWT'yi AES ile şifrelemek/deşifrelemek için anahtar【F:src/main/java/com/modernbank/authentication_service/jwt/JwtService.java†L34-L35】【F:src/main/java/com/modernbank/authentication_service/jwt/EncryptionUtil.java†L15-L34】 |
| `client.feign.account-service.*` | Account Service URL ve yolları【F:src/main/java/com/modernbank/authentication_service/api/client/AccountServiceClient.java†L9-L13】 |
| `client.feign.parameter-service.*` | Parameter Service URL ve yolları【F:src/main/java/com/modernbank/authentication_service/api/client/ParameterServiceClient.java†L13-L22】 |
| `spring.redis.host` / `spring.redis.port` | Redis bağlantısı (varsayılan `localhost:6379`, `RedisConfiguration` ile uyumlu)【F:src/main/java/com/modernbank/authentication_service/config/RedisConfiguration.java†L15-L27】 |

### Örnek `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/modernbank
    username: modernbank
    password: change-me
  jpa:
    hibernate:
      ddl-auto: update
  redis:
    host: localhost
    port: 6379

security:
  encryption:
    secret-key: changemechangeme   # 16/24/32 karakter uzunluğunda olmalı

app:
  jwtSecret: c29tZS1sb25nLWJhc2U2NC1zZWNyZXQ=
  jwtCryptoSecret: changemechangeme

client:
  feign:
    account-service:
      path: http://localhost:8081
      registerUser: /accounts/users
    parameter-service:
      path: http://localhost:8082
      getErrorCode: /parameters/error-code
      getAllErrorCodes: /parameters/error-codes
      logError: /parameters/error-log
```

### Dış servisler
- **Account Service:** Kullanıcı kayıt istekleri bu servise iletilir. Yanıt tipinin `BaseResponse` ile eşleştiğinden emin olun.【F:src/main/java/com/modernbank/authentication_service/service/impl/AuthenticationServiceImpl.java†L48-L52】
- **Parameter Service:** Hata kodu okuma ve loglama işlemleri için kullanılır. Redis önbelleği başarısız çağrılarda devreye girer.【F:src/main/java/com/modernbank/authentication_service/service/impl/ErrorCodeServiceImpl.java†L23-L63】【F:src/main/java/com/modernbank/authentication_service/exceptions/GlobalExceptionHandler.java†L28-L87】

## Projeyi Çalıştırma
```bash
# bağımlılıkları indirip uygulamayı çalıştırır
./mvnw spring-boot:run
```
Varsayılan olarak uygulama `http://localhost:8080` adresinde ayağa kalkar.

### Üretim paketlemesi
```bash
./mvnw clean package
java -jar target/authentication-service-0.0.1-SNAPSHOT.jar
```

## Testler
```bash
./mvnw test
```
Varsayılan test sınıfı Spring context’i yükleyerek temel yapılandırmayı doğrular.【F:src/test/java/com/modernbank/authentication_service/AuthenticationServiceApplicationTests.java†L6-L13】

## API Referansı
Tüm uç noktalar `AuthenticationController` üzerinden `/authentication` ile başlar.【F:src/main/java/com/modernbank/authentication_service/controller/AuthenticationController.java†L21-L49】

| Metot | URL | Açıklama |
| --- | --- | --- |
| `POST` | `/authentication/login` | Kullanıcıyı doğrular, JWT token ve rol döner. |
| `POST` | `/authentication/register` | Kullanıcı kaydı talebini account-service'e yönlendirir. |
| `POST` | `/authentication/logout` | Bearer token’ı kara listeye ekler. |
| `GET` | `/authentication/validate` | Token'ı query param üzerinden doğrular. |
| `POST` | `/authentication/validate/with-body` | Token doğrulamasını JSON gövde ile tetikler. |

### Örnek istek/yanıtlar
**Giriş**
```http
POST /authentication/login
Content-Type: application/json

{
  "email": "jane.doe@modernbank.com",
  "password": "S3curePassword!"
}
```
Yanıt:
```json
{
  "token": "<AES ile şifrelenmiş JWT>",
  "role": "CUSTOMER"
}
```

**Çıkış**
```http
POST /authentication/logout
Authorization: Bearer <AES ile şifrelenmiş JWT>
```
Yanıt:
```json
{
  "status": "SUCCESS",
  "message": "Logout successful"
}
```

**Token Doğrulama**
```http
GET /authentication/validate?token=<AES-şifreli-token>
Authorization: Bearer <AES ile şifrelenmiş JWT>
```
Yanıt:
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "jane.doe@modernbank.com",
  "authorities": ["CUSTOMER"]
}
```

## Güvenlik ve Token Yaşam Döngüsü
- `SecurityConfiguration` tüm `/authentication/**` uç noktalarına anonim erişime izin verir, diğer uç noktalar için kimlik doğrulaması ister ve oturum yönetimini `STATELESS` olarak konfigüre eder.【F:src/main/java/com/modernbank/authentication_service/config/SecurityConfiguration.java†L19-L35】
- `JwtAuthenticationFilter`, gelen Bearer token’ı AES ile çözer, kara listeyi kontrol eder ve kullanıcı detaylarını yükleyerek `SecurityContext`’e yerleştirir.【F:src/main/java/com/modernbank/authentication_service/jwt/JwtAuthenticationFilter.java†L37-L95】
- `JwtService` token ürettiğinde Base64 kodlu imzalama anahtarı kullanır ve token’ı AES ile şifreler. Token süresi varsayılan olarak 24 saattir (`EXPIREDATE`).【F:src/main/java/com/modernbank/authentication_service/jwt/JwtService.java†L23-L69】
- `BlackListServiceImpl`, token süresi kadar Redis’te TTL ayarlayarak kara liste yönetimi yapar; süre dolunca kayıt kendiliğinden silinir.【F:src/main/java/com/modernbank/authentication_service/service/impl/BlackListServiceImpl.java†L16-L35】

## Hata Yönetimi ve Gözlemleme
- `GlobalExceptionHandler`, tüm `RuntimeException` ve `BadCredentialsException` türlerini yakalayarak standart `BaseResponse` döner ve Parameter Service'e loglar.【F:src/main/java/com/modernbank/authentication_service/exceptions/GlobalExceptionHandler.java†L36-L87】
- Hata kodları `ErrorCodeConstants` altında merkezi olarak tanımlıdır ve Parameter Service üzerinden açıklamaları çekilir.【F:src/main/java/com/modernbank/authentication_service/constants/ErrorCodeConstants.java†L5-L13】
- Hata kodu servis çağrısı başarısız olursa varsayılan “Not Found” gövdesi üretilir.【F:src/main/java/com/modernbank/authentication_service/service/impl/ErrorCodeServiceImpl.java†L65-L72】

## Zamanlanmış Görevler
- `ErrorCodeServiceImpl#refreshAllErrorCodesCache` metodu her saat çalışarak tüm hata kodlarını Parameter Service'den çekip Redis önbelleğini günceller. Başarısızlık durumunda mevcut önbelleği korur ve loglama yapar.【F:src/main/java/com/modernbank/authentication_service/service/impl/ErrorCodeServiceImpl.java†L43-L63】

## Faydalı Maven Komutları
- `./mvnw spring-boot:run` – Uygulamayı geliştirme modunda başlatır.
- `./mvnw clean package` – Uygulamayı derleyip jar paketini oluşturur.
- `./mvnw test` – Tüm testleri çalıştırır.
- `./mvnw dependency:tree` – Bağımlılık ağacını görüntüler.

---
ModernBank Authentication Service hakkında daha fazla bilgi veya katkı için ekibi bilgilendirin. Sorun bildirmek veya yeni özellik talep etmek için issue açabilirsiniz.
