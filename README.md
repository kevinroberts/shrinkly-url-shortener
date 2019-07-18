# Shrinkly
Yet another URL shortening service -- Y.A.S.S.! 🙌

## Tech Stack
* Spring Boot + Spring Security Application
	w/ JPA Data Persistence (Java)
* Persistence:
Postgres and Redis for URL short code caching
* Containerized with Docker
* Continuous Integration with CircleCI

## Features

Self User registration with compulsory email verification process.

Minimum password complexity requirements using the [Passay Library](http://www.passay.org/)

### URL shortening 

* Custom URL short codes with an option for timed deletion.
* Short codes support unicode / emojis
* Shorten URLs as well as mailto: and tel: links
* Custom URL analytics with click statistics, country of origin, referrer information, etc.

### License

This is an open sourced side project by Kevin Roberts, licensed **GNU GPLv3**