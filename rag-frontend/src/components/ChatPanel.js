import React, { useState, useEffect, useRef } from 'react';
import '../styles/ChatPanel.css';

function ChatPanel({ documentId, onConversationCreated, selectedConversation }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [sources, setSources] = useState([]);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (selectedConversation) {
      // Display selected conversation
      setMessages([
        {
          type: 'user',
          text: selectedConversation.userMessage,
          sources: JSON.parse(selectedConversation.context || '[]')
        },
        {
          type: 'assistant',
          text: selectedConversation.assistantResponse,
          sources: []
        }
      ]);
    } else {
      setMessages([]);
    }
  }, [selectedConversation]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async () => {
    if (!input.trim()) return;

    const userMessage = input;
    setInput('');
    setMessages(prev => [...prev, { type: 'user', text: userMessage, sources: [] }]);
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/chat/query', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: userMessage,
          topK: 5
        })
      });

      if (!response.ok) {
        throw new Error('Chat request failed');
      }

      const data = await response.json();
      setMessages(prev => [...prev, {
        type: 'assistant',
        text: data.response,
        sources: data.sources
      }]);
      setSources(data.sources);
      onConversationCreated();
    } catch (err) {
      setMessages(prev => [...prev, {
        type: 'error',
        text: 'Error: ' + err.message,
        sources: []
      }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chat-panel">
      <h2>Chat</h2>
      <div className="messages-container">
        {messages.length === 0 ? (
          <div className="no-messages">
            <p>Upload a PDF and start chatting!</p>
          </div>
        ) : (
          messages.map((msg, idx) => (
            <div key={idx} className={`message message-${msg.type}`}>
              <div className="message-content">{msg.text}</div>
              {msg.sources && msg.sources.length > 0 && (
                <div className="sources">
                  <strong>Sources:</strong>
                  {msg.sources.map((src, i) => (
                    <div key={i} className="source-item">
                      {src.substring(0, 100)}...
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>
      <div className="input-area">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          placeholder="Ask a question..."
          disabled={loading}
        />
        <button onClick={handleSendMessage} disabled={loading}>
          {loading ? 'Sending...' : 'Send'}
        </button>
      </div>
    </div>
  );
}

export default ChatPanel;
