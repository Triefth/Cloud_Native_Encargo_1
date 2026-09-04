import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import ServicesHealthMonitor from '../components/ServicesHealthMonitor';

vi.mock('../services/api', () => ({
  apiRequest: vi.fn().mockImplementation((endpoint) => {
    return Promise.resolve({ error: null, status: 200, message: 'OK' });
  })
}));

describe('ServicesHealthMonitor Component', () => {
  it('renders title and 8 microservices cards', async () => {
    render(<ServicesHealthMonitor />);

    expect(screen.getByText(/Monitor de Salud & Resiliencia de Microservicios/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/8 \/ 8 Microservicios Activos/i)).toBeInTheDocument();
      expect(screen.getByText('BFF / API Gateway')).toBeInTheDocument();
      expect(screen.getByText('Agenda de Citas')).toBeInTheDocument();
      expect(screen.getByText('Consultas en Línea')).toBeInTheDocument();
      expect(screen.getByText('Fichas Médicas')).toBeInTheDocument();
      expect(screen.getByText('Notificaciones')).toBeInTheDocument();
      expect(screen.getByText('Informes Operativos')).toBeInTheDocument();
      expect(screen.getByText('Maestro de Usuarios')).toBeInTheDocument();
      expect(screen.getByText('Clínicas Rurales')).toBeInTheDocument();
    });
  });
});
