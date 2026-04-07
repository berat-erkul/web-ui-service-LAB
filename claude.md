# Web-UI — Claude Execution Guide (Week 4 + Week 5)

Bu doküman, workspace **(`/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_`)** için **4. hafta** ve **5. hafta** talimatlarını içerir. Aşağıdaki **Week 5** bölümü, PDF’deki 5. hafta kapsamına göre güncellenmiştir.

---

# Week 4 (Web-UI) — Claude Execution Guide

Bu bölüm, 4. hafta gereksinimleri için Claude’a verilecek **çalıştırılabilir talimat + prompt** setidir.

## Hedef (PDF Week 4)

- Spring Boot + Thymeleaf + Bootstrap 5 ile **Web-UI servisi**
- **Keycloak OAuth2 Login** (Authorization Code)
- Web-UI’nin **Gateway’e REST çağrısı yapan servis katmanı** (token’ı taşıyarak)
- Thymeleaf **layout şablonu**, **dashboard**
- **kullanıcı listeleme** ve **proje listeleme** sayfaları
- Hafta sonu çıktısı: **Web’den login olunup listeler görüntülenebilir**

## Mutlak Kurallar

- **Asla** `infisical` komutu çalıştırma.
- **Asla** `env`, `printenv`, `set` komutlarını çalıştırma.
- **Asla** `.env` gibi secret içeren dosyaları okuma.
- Her “değişiklik grubu”ndan sonra **git commit at**.
  - Commit mesajı **İngilizce** olsun.
  - Bir değişiklik grubunda birden fazla konu varsa böl ve ayrı commit’le.
- Yeni Web-UI servisini eklerken mevcut mikroservislerin isimlendirme tarzına ve port/config yaklaşımına uy.

## Repo/Proje Durumu (Varsayımlar)

Bu workspace’te backend mikroservisler mevcut:

- `Config-Service`
- `Discovery-Service`
- `Gateway-Service`
- `User-Service`
- `Project-Service`
- `Task-Service`

Web-UI servisi henüz yok (Thymeleaf `templates/` bulunamadı). Bu yüzden Week 4 için **yeni bir servis** eklenecek.

> Not: Eğer sizde “orijinal Web-UI” başka bir dizindeyse, bu dokümandaki adımlar aynı kalır; sadece “copy/adapt” ile hızlandırılır.

## Hızlı Mimari Kararları (Uygulanacak)

- Web-UI: Spring Boot MVC + Thymeleaf
- UI: Bootstrap 5 (CDN) + basit navbar/sidebar layout
- Security: `spring-boot-starter-oauth2-client` ile `oauth2Login()`
- Gateway çağrıları: Spring `WebClient`
  - OAuth2 token’ı otomatik eklemek için `ServletOAuth2AuthorizedClientExchangeFilterFunction`
- Sayfalar: `/dashboard`, `/users`, `/projects`
- Basit navigasyon: dashboard + liste sayfaları (Week 4 kapsamı)

## Uygulama Planı (Claude’un takip edeceği sıra)

### 0) Mevcut endpoint’leri doğrula (okuma)

Amaç: Web-UI’nin Gateway üzerinden hangi URL’leri çağıracağını netleştirmek.

- Gateway routing config’lerinde `User-Service` ve `Project-Service` route’larını bul.
- `UserController` ve `ProjectController` sınıflarında `@RequestMapping` ve `@GetMapping` yollarını çıkar.
- Beklenen liste endpoint’leri tipik olarak:
  - Users: `GET /api/v1/users` (veya `/users`)
  - Projects: `GET /api/v1/projects` (veya `/projects`)

Çıktı: Web-UI’de kullanacağın iki adet “base path” kesinleşsin.

**COMMIT yok** (sadece okuma).

### 1) Yeni servis iskeleti oluştur: `Web-UI-Service/web-ui-service`

Yeni Maven Spring Boot projesi oluştur.

- Group/artifact: mevcut projelerdeki örüntüyü takip et (örn. `com.cydeo`)
- Dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-thymeleaf`
  - `spring-boot-starter-security`
  - `spring-boot-starter-oauth2-client`
  - `spring-boot-starter-webflux` (sadece `WebClient` için)
  - `thymeleaf-extras-springsecurity6` (opsiyonel; sadece template içinde auth bilgisi göstermek istersen)
  - (opsiyonel) `lombok`
- `application.yml`:
  - `server.port` (çakışmayan bir port seç)
  - `gateway.base-url` (örn `http://localhost:8080`)
  - `spring.security.oauth2.client.registration.keycloak.*`
  - `spring.security.oauth2.client.provider.keycloak.issuer-uri`

**Commit message (English)**:

`chore(web-ui): scaffold Spring Boot Thymeleaf UI service`

### 2) OAuth2 Login + güvenlik temelini kur

- Security config:
  - `oauth2Login()` etkin
  - `logout` Keycloak yönlendirmesi gerekiyorsa ekle (minimum viable: local logout)
  - `authorizeHttpRequests`:
    - `/css/**`, `/js/**`, `/images/**` permit
    - `/` -> `/dashboard` redirect veya permit
    - `/dashboard`, `/users`, `/projects` authenticated
- Not: Week 4 için rol bazlı menü/görünüm şart değil; sadece login + sayfaya erişim yeterli.

**Commit message (English)**:

`feat(web-ui): add Keycloak OAuth2 login and basic route protection`

### 3) Gateway çağrıları için WebClient katmanını kur

Hedef: Kullanıcının login sonrası aldığı access token ile Gateway’e istek atmak.

- `WebClient` bean:
  - `ServletOAuth2AuthorizedClientExchangeFilterFunction` ile OAuth2 token’ı otomatik bağla
- DTO’lar:
  - Backend’in döndürdüğü `ResponseWrapper` (ve varsa `data` alanı) yapısına göre `ResponseWrapper<T>` oluştur
  - `UserDto`, `ProjectDto` (liste için gerekli alanlar)
- Service layer:
  - `UserGatewayClient` (veya `UserServiceClient`) -> `getUsers()`
  - `ProjectGatewayClient` -> `getProjects()`
- Controller’lar:
  - `UserViewController` -> model’e `users` koy, `users/list`
  - `ProjectViewController` -> model’e `projects` koy, `projects/list`

**Commit message (English)**:

`feat(web-ui): add token-propagating WebClient clients for gateway calls`

### 4) Thymeleaf layout + dashboard + listeleme sayfaları

- `templates/layout.html` (fragment: navbar/sidebar + content)
- `templates/dashboard.html`
- `templates/users/list.html`
- `templates/projects/list.html`
- Bootstrap 5 CDN, responsive table, empty state, basit header

**Commit message (English)**:

`feat(web-ui): add Thymeleaf layout, dashboard, and list pages`

### 5) Çalıştırma doğrulaması (lokalde)

Amaç: “Login -> listeleri gör” senaryosu.

- Servisleri ayağa kaldır:
  - Config, Discovery, Gateway, User, Project, Keycloak, Web-UI
- Browser:
  - `http://localhost:<web-ui-port>/dashboard` -> Keycloak login
  - Login sonrası `/users` ve `/projects` sayfalarında liste görünüyor mu

**Commit yok** (sadece doğrulama).

## Minimum Dosya/Klasör Yapısı (Beklenen)

- `Web-UI-Service/web-ui-service/pom.xml`
- `Web-UI-Service/web-ui-service/src/main/java/...`
  - `config/SecurityConfig.java`
  - `config/WebClientConfig.java`
  - `controller/DashboardController.java`
  - `controller/UserViewController.java`
  - `controller/ProjectViewController.java`
  - `client/UserGatewayClient.java`
  - `client/ProjectGatewayClient.java`
  - `dto/*`
  - `wrapper/ResponseWrapper.java`
- `Web-UI-Service/web-ui-service/src/main/resources/templates/...`
- `Web-UI-Service/web-ui-service/src/main/resources/application.yml`

## Claude’a Kopyala-Yapıştır Promptları

> Aşağıdaki promptları **sırayla** kullan. Her prompt sonunda “done + commit hash” raporla.

### Prompt 1 — Discover endpoints (read-only)

```text
You are working in this workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Goal: Implement Week 4 Web-UI (Spring Boot + Thymeleaf + Bootstrap 5) with Keycloak OAuth2 login and gateway-backed user/project list pages.

Step 1 (read-only): Identify the exact gateway routes and backend endpoints for listing users and projects.
- Inspect gateway configuration (routes) and the User/Project controllers to extract the correct GET endpoints.
- Output: the final gateway URLs for "list users" and "list projects", and the ResponseWrapper JSON shape.

Constraints:
- Do NOT run infisical.
- Do NOT run env/printenv/set.
- Do NOT read .env files.
- No code changes yet, no commits.
```

### Prompt 2 — Scaffold Web-UI service + first commit

```text
Create a new Spring Boot service under:
Web-UI-Service/web-ui-service

Use Spring MVC + Thymeleaf + Spring Security + OAuth2 Client + WebClient (webflux).
Use Bootstrap 5 via CDN. (Optionally add thymeleaf-extras-springsecurity6 only if needed.)

Add application.yml with placeholders for:
- server.port (non-conflicting)
- gateway.base-url (http://localhost:<gateway-port>)
- spring.security.oauth2.client.registration.keycloak.*
- spring.security.oauth2.client.provider.keycloak.issuer-uri

After the scaffold builds, commit with an English message:
"chore(web-ui): scaffold Spring Boot Thymeleaf UI service"

Constraints:
- Do NOT run infisical.
- Do NOT run env/printenv/set.
- Do NOT read .env files.
```

### Prompt 3 — OAuth2 login + security config + commit

```text
Implement Keycloak OAuth2 login for the Web-UI service.
- Protect /dashboard, /users, /projects as authenticated.
- Permit static paths.
- Add basic logout (minimum viable ok).

Commit with:
"feat(web-ui): add Keycloak OAuth2 login and basic route protection"
```

### Prompt 4 — WebClient clients calling Gateway with token + commit

```text
Implement a token-propagating WebClient setup in Web-UI:
- Configure WebClient with ServletOAuth2AuthorizedClientExchangeFilterFunction.
- Create DTOs for User and Project list views.
- Create a generic ResponseWrapper<T> matching backend JSON.
- Implement gateway client classes to call the exact list endpoints discovered earlier.
- Wire controllers to fetch lists and render templates.

Commit with:
"feat(web-ui): add token-propagating WebClient clients for gateway calls"
```

### Prompt 5 — Thymeleaf layout + pages + commit

```text
Create Thymeleaf templates:
- layout.html (fragments)
- dashboard.html
- users/list.html
- projects/list.html

Use Bootstrap 5 tables, responsive layout, and a simple navigation menu.

Commit with:
"feat(web-ui): add Thymeleaf layout, dashboard, and list pages"
```

### Prompt 6 — Manual test checklist (no commit)

```text
Provide a concise manual test plan to verify:
- Web-UI redirects to Keycloak login when unauthenticated.
- After login, /users and /projects pages display data fetched via gateway with the user token.
- Handle empty/error states gracefully (show a banner/message, not a stacktrace).

No code changes, no commit.
```

---

# Week 5 (Web-UI) — CRUD + rol bazlı görünüm — Claude Execution Guide

## Hedef (PDF Week 5)

- **Kullanıcı / proje / görev** için **oluşturma, güncelleme, silme** formları ve akışları (Thymeleaf)
- **Rol bazlı menü** ve **erişim kontrolü** (UI’da gizleme + sunucu tarafında URL/metot koruması)
- **Form validasyonu** (Bean Validation) ve **anlaşılır hata mesajları** (backend `ExceptionWrapper` / validation listesi + MVC `BindingResult`)
- Hafta sonu: **Admin, Manager, Employee** rolleri web üzerinden kendi yetkileri dahilinde işlem yapabilmeli

## Mutlak kurallar (Week 5 — kullanıcı talebi)

- **Asla `git commit` çalıştırma** (Week 5 görevleri bitene kadar). `git add`, `commit`, `push` yok.
- **Sadece hedef proje değiştirilebilir:** yalnızca  
  `Web-UI-Service/web-ui-service/`  
  altındaki dosyalar (ve bu modülün içinde yeni dosyalar).
- **Asla** şu “base” projelerde kod/config değişikliği yapma:  
  `Config-Service/`, `Config-Repo/`, `Discovery-Service/`, `Gateway-Service/`, `User-Service/`, `Project-Service/`, `Task-Service/`, `docker-compose*.yml` (kök veya başka yer), vb.  
  İhtiyaç duyulan API sözleşmesi için **salt okunur** inceleme yapılabilir; değişiklik yok.
- **Asla** `infisical` çalıştırma; **asla** `env` / `printenv` / `set`; **asla** `.env` veya secret dosyası okuma.

## Mimari notlar

- Tüm mutasyonlar **Gateway** üzerinden mevcut REST uçlarına gider (Week 4’teki gibi token taşıyan `WebClient`).
- Gateway route önekleri (okuma — değiştirme yok):  
  `/user-service/**` → User-Service, `/project-service/**` → Project-Service, `/task-service/**` → Task-Service.
- Backend çoğu uçta **`@RolesAllowed(...)`** kullanıyor (ör. User tarafında `Admin`, Project’te `Admin`+`Manager`, Task’ta `Manager` / `Employee` vb.). Web-UI’da menü ve `SecurityFilterChain` / `@PreAuthorize` bu gerçeklerle **aynı hizada** olmalı; fazla yetki göstermek UX hatası, eksik koruma güvenlik hatası.
- Keycloak JWT içindeki realm rollerinin Spring Security’de `hasRole('Admin')` gibi ifadelerle çalışması için Web-UI’da genelde **authority eşlemesi** gerekir (ör. `ROLE_` öneki veya özel `GrantedAuthoritiesMapper`). Bunu **sadece Web-UI** içinde çöz.

## Uygulama sırası (özet)

1. Salt okunur: tüm CRUD uçları + roller + istek gövdesi alanları (DTO okuma).
2. Web-UI: ortak altyapı (validation dependency, hata modeli, WebClient mutasyon + hata gövdesi okuma).
3. Web-UI: güvenlik (rol eşlemesi + `authorizeHttpRequests` / method security).
4. Web-UI: User CRUD sayfaları.
5. Web-UI: Project CRUD sayfaları.
6. Web-UI: Task CRUD sayfaları (proje bağlamı, gerekli listeler).
7. Web-UI: menü/dashboard iyileştirme ve manuel test listesi.

---

## Week 5 — Claude’a kopyala-yapıştır promptları (sırayla)

> Her prompt sonunda: yapılanların özeti + değişen dosya yolları. **Commit atma.**

### Prompt 1 — Salt okunur keşif (CRUD + roller + DTO)

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Week 5 scope: Web-UI CRUD + role-based UI and server-side access control.

READ ONLY. Do not modify any files.

Tasks:
1) From GatewayConfig (read only), confirm path prefixes for user-service, project-service, task-service.
2) Read UserController, ProjectController, TaskController (READ ONLY). Produce a table:
   - HTTP method + full downstream path under /api/v1/...
   - equivalent Gateway URL (e.g. /user-service/api/v1/...)
   - @RolesAllowed for each endpoint
3) For each CRUD operation needed by Week 5, note request body type (UserDTO / ProjectDTO / TaskDTO) and required JSON fields by reading those DTO classes (READ ONLY from backend modules).
4) Note ResponseWrapper and ExceptionWrapper JSON shapes used on success/error (read wrapper classes READ ONLY).

Hard constraints:
- Do not change any code outside Web-UI-Service/web-ui-service/ in later prompts; for this prompt, no code changes at all.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Output: a single markdown report you will use in the next prompts.
```

### Prompt 2 — Web-UI: validation + paylaşılan hata yardımcıları

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Goal: prepare Week 5 foundations inside Web-UI only.

Implement:
1) Add spring-boot-starter-validation if not already present.
2) Add form/command model classes (or extend DTOs) for User/Project/Task create & update with Bean Validation annotations matching backend constraints as closely as possible (required fields, sizes, etc. — infer from backend DTOs).
3) Add ExceptionWrapper (+ nested validation error types if present in backend) mirroring backend JSON for friendly error display.
4) Add a small helper to parse WebClient 4xx/5xx responses into a user-facing message (including validation field errors when returned).

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 3 — Web-UI: WebClient mutasyonları (POST/PUT/DELETE) + GatewayClient genişletme

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Using the endpoint table from Prompt 1, extend UserGatewayClient, ProjectGatewayClient, and add TaskGatewayClient:
- Implement create, update, delete, and any extra GETs needed for edit forms (e.g. read by code/username).
- Use the existing token-propagating WebClient bean.
- Return structured results to controllers (success data vs error wrapper/message), avoid throwing raw stack traces to the UI.

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 4 — Web-UI: Keycloak rolleri → Spring authorities + SecurityFilterChain

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Goal: role-based access for Web-UI routes consistent with backend @RolesAllowed.

Implement:
1) Map Keycloak realm roles to Spring Security granted authorities so templates can use sec:authorize and server-side checks can use hasRole / hasAnyRole consistently (e.g. Admin, Manager, Employee — match backend spelling).
2) Update SecurityConfig:
   - Keep oauth2Login and logout behavior working.
   - Protect new MVC routes (e.g. /users/**, /projects/**, /tasks/**) with rules that mirror backend capabilities as closely as possible.
3) If method-level security is needed, enable it in a minimal way (@EnableGlobalMethodSecurity or equivalent for the Spring Boot version in this project) ONLY inside Web-UI.

Optional: add OAuth2 scopes for roles in application.yml ONLY inside web-ui-service if required for roles to appear in the token — do not change Keycloak itself here (no base project changes).

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 5 — Web-UI: User CRUD Thymeleaf ekranları

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Implement User CRUD pages (Admin-aligned with backend):
- List (existing) + create form + edit form + delete (POST or POST with _method if you use hidden field; prefer clear explicit endpoints in MVC).
- Use layout fragment pattern already used in Week 4.
- Bootstrap 5 forms, field errors from BindingResult, and banner for API errors from ExceptionWrapper.
- Hide/disable actions in UI for users without Admin where backend is Admin-only.

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 6 — Web-UI: Project CRUD Thymeleaf ekranları

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Implement Project CRUD pages consistent with backend roles (Admin + Manager where applicable):
- Use correct list endpoints for role (admin vs manager lists) if backend exposes both; otherwise document assumption in code comments briefly.
- Create/update/delete/start/complete flows only if backend supports them and roles allow — do not invent new API paths.

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 7 — Web-UI: Task CRUD Thymeleaf ekranları

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Implement Task CRUD pages:
- Task list should be reachable in a sensible UX (e.g. by project code).
- Wire create/update/delete (and employee-specific update endpoints if required by backend) according to TaskController roles.
- Strong validation messages and API error mapping.

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 8 — Web-UI: rol bazlı menü + dashboard + tutarlılık

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

Modify ONLY:
Web-UI-Service/web-ui-service/

Polish:
1) Sidebar/menu: show links only for roles that can use them (sec:authorize).
2) Dashboard: short role-based summary cards or links to relevant sections.
3) Ensure CSRF works with all forms (Spring Security defaults).
4) Consistent flash messages for success operations.

Constraints:
- Touch ONLY Web-UI-Service/web-ui-service/.
- Do NOT run git commit.
- Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.

Report changed file paths when done.
```

### Prompt 9 — Manuel test planı (kod değişikliği yok)

```text
Workspace:
/Users/beraterkul/Documents/WORK/WebMobil Lecture/LAB_Microservices_

No code changes. Do NOT run git commit.

Produce a manual test matrix by role (Admin, Manager, Employee):
- Which pages are visible
- CRUD operations expected to succeed vs 403 from backend
- Validation error UX checks
- Logout/login retest

Do NOT run infisical; do NOT run env/printenv/set; do NOT read .env files.
```

