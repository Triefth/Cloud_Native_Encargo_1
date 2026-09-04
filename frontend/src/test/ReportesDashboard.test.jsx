import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import ReportesDashboard from '../components/ReportesDashboard';

vi.mock('../services/api', () => ({
  reportesApi: {
    getResumen: vi.fn().mockResolvedValue({
      error: null,
      data: {
        disponibilidadGlobal: 99.98,
        latenciaPromedioMs: 38,
        totalAtencionesRealizadas: 250,
        tasaNoShow: 1.5,
        totalMicroserviciosActivos: 8
      }
    }),
    getAll: vi.fn().mockResolvedValue({
      error: null,
      data: [
        { id: 1, modulo: 'citas-service', tipoEvento: 'CITA_AGENDADA', descripcion: 'Cita agendada', timestamp: '2026-09-03T10:00:00' }
      ]
    })
  }
}));

describe('ReportesDashboard Component', () => {
  it('renders dashboard title and SLA KPIs correctly', async () => {
    render(<ReportesDashboard />);

    expect(screen.getByText('Informes Operativos & Dashboard de Analítica')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText(/99.98%/i)).toBeInTheDocument();
      expect(screen.getByText(/38 ms/i)).toBeInTheDocument();
      expect(screen.getByText('250')).toBeInTheDocument();
      expect(screen.getByText(/1.5%/i)).toBeInTheDocument();
    });
  });

  it('renders event audit logs table', async () => {
    render(<ReportesDashboard />);

    await waitFor(() => {
      expect(screen.getByText('CITA_AGENDADA')).toBeInTheDocument();
      expect(screen.getByText('citas-service')).toBeInTheDocument();
    });
  });
});
