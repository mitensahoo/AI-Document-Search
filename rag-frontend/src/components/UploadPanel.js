import React, { useState } from 'react';
import '../styles/UploadPanel.css';

function UploadPanel({ onUploaded }) {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
    setError('');
    setMessage('');
  };

  const handleUpload = async () => {
    if (!file) {
      setError('Please select a file');
      return;
    }

    setUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('http://localhost:8080/api/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('Upload failed');
      }

      const data = await response.json();
      setMessage(`File uploaded! Document ID: ${data.documentId}, Chunks: ${data.totalChunks}, Embedded: ${data.embeddedChunks}`);
      setFile(null);
      document.querySelector('input[type="file"]').value = '';
      onUploaded(data.documentId);
    } catch (err) {
      setError('Error uploading file: ' + err.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="upload-panel">
      <h2>Upload PDF</h2>
      <div className="upload-form">
        <input 
          type="file" 
          accept=".pdf"
          onChange={handleFileChange}
          disabled={uploading}
        />
        <button 
          onClick={handleUpload}
          disabled={uploading || !file}
        >
          {uploading ? 'Uploading...' : 'Upload'}
        </button>
      </div>
      {message && <div className="success-message">{message}</div>}
      {error && <div className="error-message">{error}</div>}
    </div>
  );
}

export default UploadPanel;
