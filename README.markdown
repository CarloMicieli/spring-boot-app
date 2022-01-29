# spring boot app


## How to run

To run the application using `gradle` or `docker`, a running postgres instance must be available on localhost.

This command will run **postgres** inside a docker container:

```bash
  docker run -it --rm -p:5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_DB=sampledb postgres
```

To run the application using `gradle`:

```bash
  ./gradlew bootRun --args='--spring.profiles.active=local'
```

```bash
  ./gradlew clean bootBuildImage
  docker-compose up
```

```bash
  pip install -U httpie
  pip install -U httpie-jwt-auth
```


### Docker compose

```
$ docker-compose build
$ docker-compose up -d
```

To stop the application:

```
$ docker-compose down
```

## Test the web api

```
$ pip install -U httpie
$ pip install -U httpie-jwt-auth
```

Create a new user

```
$ http POST :8080/api/auth/register username=george password=Stephenson
HTTP/1.1 200 
```

in order to make api calls, it is required a JWT token. To get a token is required for the user to make a login:

```
$ http POST :8080/api/auth/signin username=george password=Stephenson
HTTP/1.1 204 
Authorization: eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjaWNjaW5hMiIsImlzcyI6ImV4YW1wbGUuaW8iLCJpYXQiOjE2MDU0Mjk3OTAsImV4cCI6MTYwNjAzNDU5MH0.PI40WTav4GwKvGYRdXujZh00NKk40CCQkXXrZY4Q4txrYv279edEwuIE9gc3vZPI00dl9_5hy3L_cMRXhZlZbQ
```

```
$ export JWT_AUTH_TOKEN=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiO....
$ http --auth-type=jwt :8080/api/collections
HTTP/1.1 200 
```

```
$ http --auth-type=jwt POST :8080/api/catalogItems brand=ACME itemNumber=1234 description="My first item" category="C"
HTTP/1.1 201 
Location: http://localhost:8080/api/catalogItems/4
```

```
$ http --auth-type=jwt :8080/api/catalogItems/4
HTTP/1.1 200 
Content-Type: application/hal+json

{
    "_links": {
        "self": {
            "href": "http://localhost:8080/api/catalogItems/4"
        }
    },
    "catalogItem": {
        "brand": "ACME",
        "category": "C",
        "description": "My first item",
        "id": 4,
        "itemNumber": "1234"
    }
}
 
```