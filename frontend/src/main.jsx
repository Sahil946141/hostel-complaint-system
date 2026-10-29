import { createRoot } from 'react-dom/client';
import App from './App.jsx';

const rootElement = document.getElementById('app');

if (!rootElement) {
  throw new Error('The application root element was not found.');
}

createRoot(rootElement).render(<App />);
