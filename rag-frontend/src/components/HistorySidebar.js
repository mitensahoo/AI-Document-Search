import React from 'react';
import '../styles/HistorySidebar.css';

function HistorySidebar({ conversations, onSelect, selectedId }) {
  return (
    <div className="history-sidebar">
      <h3>Conversation History</h3>
      <div className="history-list">
        {conversations.length === 0 ? (
          <p className="no-history">No conversations yet</p>
        ) : (
          conversations.map(conv => (
            <div
              key={conv.id}
              className={`history-item ${selectedId === conv.id ? 'selected' : ''}`}
              onClick={() => onSelect(conv)}
            >
              <div className="history-message">
                {conv.userMessage.substring(0, 40)}...
              </div>
              <div className="history-time">
                {new Date(conv.createdAt).toLocaleString()}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default HistorySidebar;
