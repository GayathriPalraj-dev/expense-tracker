# Spendwise · Expense Tracker

A small personal finance app built with React, Spring Boot, and MongoDB. It supports registration and login, private income and expense entries, and a totals dashboard.

## Run locally

You need Java 21, Maven, Node.js 18+, and a MongoDB Atlas database (or a local MongoDB instance).

1. In Atlas, create a database user, allow your development IP in Network Access, and copy the application connection string.
2. In `backend`, copy `.env.example` to `.env` and provide `MONGODB_URI` and a base64-encoded `JWT_SECRET`. For example, generate a strong key with `openssl rand -base64 32`. Keep the secret private. Set `PORT=8080` and `FRONTEND_URL=http://localhost:5173`.
3. Export those variables in your terminal, then run `mvnw.cmd spring-boot:run` from `backend` on Windows (or `./mvnw spring-boot:run` on macOS/Linux). The checked-in Maven Wrapper pins Maven and downloads it on first use, so a separate Maven installation is not required. Spring reads environment variables; it does not automatically load a `.env` file.
4. In `frontend`, copy `.env.example` to `.env` if you want to override the API URL. Run `npm install` and `npm run dev`.
5. Open the Vite URL shown in the terminal (usually `http://localhost:5173`), register, and add an entry.

## Deploy

### Backend on Render

Push this project to a Git repository and create a Render Blueprint using `render.yaml` (or create a Docker web service with `backend` as the root directory). Add `MONGODB_URI`, a base64-encoded `JWT_SECRET` of at least 32 random bytes, and `FRONTEND_URL` containing the deployed frontend's exact origin (for example, `https://your-app.vercel.app`). The Docker image listens on port 8080. Render assigns `PORT`; Spring uses it when present. The health check uses Spring Actuator's public health endpoint.

### Frontend on Vercel

Import the repository into Vercel and set the project root to `frontend`. Set `VITE_API_URL` to the backend origin plus `/api`, such as `https://your-api.onrender.com/api`, then deploy. `vercel.json` supports client-side route fallback.

After deploying, set Render's `FRONTEND_URL` to the Vercel origin and redeploy/restart the backend. Also permit the Render service to reach your Atlas cluster. Use placeholders in `.env.example`; never commit real credentials.

## Security notes

Passwords are stored as BCrypt hashes. The API reads the account ID from a signed, expiring JWT and scopes transaction reads and writes to that account. The browser keeps the bearer token in `localStorage` for this beginner MVP; this is convenient but JavaScript-accessible, so a future production hardening step is moving tokens to secure, HttpOnly cookies with CSRF protections. Use HTTPS in production and a unique, random JWT secret.

## API

- `POST /api/auth/register`, `POST /api/auth/login`
- `GET/POST /api/transactions`, `GET/PUT/DELETE /api/transactions/{id}`
- `GET /api/dashboard/summary`

Transaction and dashboard endpoints require `Authorization: Bearer <token>`. Each transaction amount is a positive decimal; type determines whether it counts as income or expense.
