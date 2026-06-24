# Shrinkly
Yet another URL shortening service -- Y.A.S.S.!


Spring Boot + Spring Security based Application
- With
JPA Data Persistence
- Persistence Layers:
Postgres and Redis 

- Self User registration with compulsory email verification process. With Email validation best practices

---
*Security & Architecture Notes*

- CSRF protection is enabled. The token is rendered into `<meta>` tags in `fragments/header`
  and attached to every jQuery AJAX request by a global `ajaxSend` hook in `static/js/shrinkly.js`;
  Thymeleaf `th:action` forms get a hidden `_csrf` field automatically. Logout is matched on
  GET so the existing logout links/redirect keep working.
- Click counts are tracked in Redis with an atomic `INCR` on the redirect hot path (no
  synchronous DB write per hit). The Postgres `clicks` column is reconciled from Redis in
  batch by `ClickCountReconcileTask` (`clicks.reconcile.cron.expression`, every 10 min by
  default); listings read the live count from Redis.
- Front-end CDN libraries (jQuery 3.7.1, Popper 1.16.1, Bootstrap 4.6.2, Handlebars 4.7.8,
  lodash 4.17.21, clipboard 2.0.11) are pinned and loaded with Subresource Integrity hashes.

---
*Toolchain*

- Java 25, Spring Boot 4.1 (Spring Framework 7, Spring Security 7, Hibernate 7, Jackson 3)
- Build: `./mvnw package` (the Maven wrapper downloads Maven 3.9.x)
- Docker image: `docker build -t vinberts/shrinkly .` (the image build runs the full Maven build internally)
- CI publishing requires `DOCKERHUB_USER` / `DOCKERHUB_PASS` environment variables in CircleCI

---
*Required Environment Variables For App* 

```
SPRING_PROFILES_ACTIVE=prod
POSTGRES_DB_CONNECTION_URL=
POSTGRES_DB_USER=
POSTGRES_DB_PASSWORD=
REDIS_HOST=redis
REDIS_PORT=6379
TURNSTILE_SITE_KEY=
TURNSTILE_SECRET_KEY=
SHORTCODE_CIPHER_KEY=
SPRING_MAIL_HOST=
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
SHRINKLY_BASE_URL=(host url)
SHORTCODE_CIPHER_KEY=
```
