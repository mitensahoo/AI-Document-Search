# RAG Backend (Spring Boot)

Minimal scaffold for the RAG backend with a PDF upload endpoint.

Run:

```bash
cd "e:\AI Document Search\rag-backend"
mvn spring-boot:run
```

Endpoints:
- `GET /api/health` - health check
- `POST /api/upload` - multipart file upload (form field `file`)
