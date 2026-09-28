# Digital Parcel Repository - MERN

This folder is the MERN migration of the original Spring Boot application.

## Requirements

- Node.js 18+
- MongoDB running locally or a MongoDB Atlas connection string

## Run the API

```powershell
cd server
npm install
Copy-Item .env.example .env
npm run dev
```

## Run the React client

```powershell
cd client
npm install
npm run dev
```

The Vite client runs on `http://localhost:5173` and proxies `/api` to the Express server on port 5000.

Default seeded accounts:

- Admin: `admin` / `admin123`
- Security: `security1` / `security123`

Change these credentials before using the application outside local development.
