import React, { useState, useEffect } from 'react';
import './App.css';
import UploadPanel from './components/UploadPanel';
import ChatPanel from './components/ChatPanel';
import HistorySidebar from './components/HistorySidebar';

function App() {
  const [documentId, setDocumentId] = useState(null);
  const [history, setHistory] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = () => {
    fetch('http://localhost:8080/api/chat/history')
      .then(res => res.json())
      .then(data => setHistory(data))
      .catch(err => console.error('Failed to fetch history:', err));
  };

  const handleDocumentUploaded = (docId) => {
    setDocumentId(docId);
    fetchHistory();
  };

  const handleConversationCreated = () => {
    fetchHistory();
  };

  const handleSelectConversation = (conv) => {
    setSelectedConversation(conv);
  };

  return (
    <div className="app-container">
      <div className="sidebar">
        <HistorySidebar 
          conversations={history} 
          onSelect={handleSelectConversation}
          selectedId={selectedConversation?.id}
        />
      </div>
      <div className="main-content">
        <div className="header">
          <h1>RAG Chatbot</h1>
          <p>Chat with your PDF documents</p>
        </div>
        <div className="content-grid">
          <div className="upload-section">
            <UploadPanel onUploaded={handleDocumentUploaded} />
          </div>
          <div className="chat-section">
            <ChatPanel 
              documentId={documentId} 
              onConversationCreated={handleConversationCreated}
              selectedConversation={selectedConversation}
            />
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
