# API for a blog or something like that.

# Technologies used:
- Ktor
- PostgreSQL + HikariCP

# Supports
- Authentication using a JSON Web Token
- Creating, updating posts
- Creating users

# For testing, I used
- EmbeddedPostgres

# Docker
```
docker-compose up
```

# Other
To start the server the following environment variables should be defined
- JDBC_DRIVER
- JDBC_DATABASE_URL
- SECRET_KEY
- JWT_SECRET
