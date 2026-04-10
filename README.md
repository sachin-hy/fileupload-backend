
## File Upload Backend

A scalable file upload and download backend built with Spring Boot,
supporting large files using chunk-based uploads and pre-signed URLs.
The system uses aws s3 bucket storage  and MySQL for
metadata persistence.

## Features

- Chunk-based upload and download for large files
- Pre-signed URL generation for direct client-to-storage transfer
- S3-compatible object storage integration
- Metadata storage using MySQL and JPA
- Secure APIs with Spring Security and JWT
- Backend does not stream large files through application memory


## Tech Stack

- Java 21
- Spring Boot 3
- Spring Security + JWT
- MySQL + JPA
- AWS S3 Bucket for storage
  
