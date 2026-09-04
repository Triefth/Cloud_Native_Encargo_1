import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import App from '../App';

vi.mock('../services/api', () => ({
  getAuthToken: vi.fn().mockReturnValue(null),
  setAuthToken: vi.fn(),
  authApi: {
    validateToken: vi.fn().mockResolvedValue({ status: 200, data: { valid: true } })
  },
  apiRequest: vi.fn().mockResolvedValue({ error: null, status: 200, data: [] }),
  reportesApi: {
    getResumen: vi.fn().mockResolvedValue({ error: null, data: {} }),
    getAll: vi.fn().mockResolvedValue({ error: null, data: [] })
  }
}));

describe('App Main Component', () => {
  it('renders navbar and footer', async () => {
    render(<App />);

    expect(screen.getByText('Telemedicina Rural')).toBeInTheDocument();
    expect(screen.getByText(/DSY1107 Desarrollo Cloud Native I/i)).toBeInTheDocument();
  });

  it('switches tabs when clicked', async () => {
    render(<App />);

    const reportesBtn = screen.getByText('Reportes Operativos');
    fireEvent.click(reportesBtn);

    await waitFor(() => {
      expect(screen.getByText('Informes Operativos & Dashboard de Analítica')).toBeInTheDocument();
    });
  });
});
