# RAG Frontend (React)

A React-based chatbot UI for the RAG backend. Users can upload PDFs, chat with them using GPT-4, and view conversation history.

## Setup

```bash
cd "e:\AI Document Search\rag-frontend"
npm install
npm start
```

The frontend will open at `http://localhost:3000`.

## Features

- **PDF Upload Panel**: Upload PDF files for chunking and embedding
- **Chat UI**: Ask questions about uploaded documents with source references
- **Conversation History**: Sidebar showing past conversations
- **Source References**: View relevant document chunks used for each response

## Configuration

The frontend connects to the backend at `http://localhost:8080`.
Make sure the backend is running before starting the frontend.

## Build for Production

```bash
npm run build
```

Output will be in the `build/` directory.
